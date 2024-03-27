import argparse
import os
import re
import shutil
import string
import sys
from collections import OrderedDict
from pathlib import Path


def extract_log(log_file):
    first = True
    raw = False
    results = False

    folds = []
    fold = {
        "name": "",
        "data": [],
        "results": ""
    }

    examples_accumulators = []
    example_accumulator = []
    results_accumulator = []

    with open(log_file, 'r') as file:
        for line in file:
            line = line.strip('\n')
            if line.startswith("======================"):
                fold_text = line.split("====================== ")[1]
                if first:
                    fold['name'] = fold_text
                    first = False
                else:
                    # store
                    fold['results'] = results_accumulator
                    folds.append(fold)
                    fold = {
                        "name": fold_text,
                        "data": [],
                        "results": ""
                    }
                    raw = False
                    results = False

            elif line.startswith("=== START RAW RESULTS ==="):
                raw = True
            elif line.startswith("=== END RAw RESULTS ==="):
                fold['data'] = examples_accumulators
                example_accumulator = []
                examples_accumulators = []
                raw = False
            elif line.startswith("===== Field-level results ====="):
                results = True
            elif len(line.strip()) == 0 and raw:
                if len(example_accumulator) > 0:
                    examples_accumulators.append(example_accumulator)
                    example_accumulator = []
            else:
                if results:
                    results_accumulator.append(line)
                elif raw:
                    splits = line.split('\t')
                    splits[len(splits) - 1].replace("\n", "")
                    example_accumulator.append(splits)

    fold['results'] = results_accumulator
    folds.append(fold)
    return folds


def extract_error_cases(input_data, tokens_before=5, tokens_after=5):
    error_cases = []
    for idx, fold in enumerate(input_data):
        # a combined list:
        # error_case[0] consists in the expected class
        # error_case[1] consists in the list of tokens
        error_case = {"expected": "", "tokens": []}
        # error_case = {"label": "", "stream": []}
        in_error = False

        for example in fold['data']:
            for i, raw_line in enumerate(example):
                expected_class_index = len(raw_line) - 2
                predicted_class_index = len(raw_line) - 1

                expected_class = raw_line[expected_class_index]
                predicted_class = raw_line[predicted_class_index].replace('\n', '')

                # if the entities are wrongly recognised (and not <other>)
                if expected_class != predicted_class:
                    reference_class = expected_class if expected_class != "<other>" else predicted_class
                    reference_class_plain = reference_class.replace("I-", "")
                    previous_reference_class = ''
                    if i > 0 and len(example[i - 1]) > 1:
                        previous_reference_class = example[i - 1][
                            expected_class_index] if expected_class != "<other>" else example[i - 1][
                            predicted_class_index]
                    previous_reference_class_plain = previous_reference_class.replace("I-", "")
                    if in_error is False:
                        if len(error_case) > 0:
                            if len(error_case['tokens']) > 0:
                                error_case['tokens'] = append_tokens_after(error_case['tokens'], example, i - 1,
                                                                           tokens_after)
                                error_cases.append(error_case)
                                error_case = {"expected": reference_class, "tokens": []}

                            if error_case['expected'] == '':
                                error_case['expected'] = reference_class

                            error_case['tokens'] = append_tokens_before(error_case['tokens'], example, i, tokens_before)

                            item_string = get_item_diff_marker(expected_class, predicted_class)
                            error_case['tokens'].append(
                                [error_case['expected'] + "|" + item_string, expected_class, predicted_class])
                            in_error = True
                        else:
                            raise Exception("This should never be reached. Boom! ")
                    else:
                        # check if the previous item is part of the same label
                        if previous_reference_class_plain == reference_class_plain and reference_class != '<other>':
                            if expected_class.startswith("I-"):
                                error_case['tokens'] = append_tokens_after(error_case[1], fold, i - 1, tokens_after)
                                error_cases.append(error_case)
                                error_case = {"expected": reference_class, "tokens": []}
                                error_case['tokens'] = append_tokens_before(error_case[1], fold, i, tokens_before)

                            item_string = get_item_diff_marker(expected_class, predicted_class)
                            error_case['tokens'].append([raw_line[0] + item_string, expected_class, predicted_class])
                        else:
                            error_case['tokens'] = append_tokens_after(error_case[1], fold, i - 1, tokens_after)
                            error_cases.append(error_case)
                            error_case = {"expected": reference_class, "tokens": []}
                            error_case['tokens'] = append_tokens_before(error_case[1], fold, i, tokens_before)
                            item_string = get_item_diff_marker(expected_class, predicted_class)
                            error_case['tokens'].append([raw_line[0] + item_string, expected_class, predicted_class])
                else:
                    if in_error is True:
                        in_error = False
                        if len(error_case) > 0:
                            if len(error_case[1]) > 0:
                                error_case['tokens'] = append_tokens_after(error_case['tokens'], example, i - 1,
                                                                           tokens_after)
                                error_cases.append(error_case)
                                error_case = {"expected": "", "tokens": []}
                        else:
                            print("Perhaps something wrong")

                    else:
                        pass

                if len(raw_line) == 0:
                    print("Line is empty. Something is wrong. ")
                if in_error:
                    in_error = False
                    # error_case.append("|")
                    error_cases.append(error_case)
                    error_case = {"expected": "", "tokens": []}

        if len(error_case) > 0 and len(error_case['tokens']) > 0:
            # error_case.append(['|', '', ''])
            error_cases.append(error_case)

    return error_cases


def append_tokens_before(error_case_tokens, example, i, nb_tokens_to_include):
    reached_annotation_beginning = False
    if i > nb_tokens_to_include - 1 and len(example[i - 1]) > 1:
        current_item = example[i]
        current_expected_class = current_item[len(current_item) - 2]
        if current_expected_class.startswith("I-"):
            reached_annotation_beginning = True

        for x in range(i - 1, i - nb_tokens_to_include - 1, -1):
            item = example[x]
            if len(item) <= 1:
                error_case_tokens = []
                continue

            expected_class = item[len(item) - 2]
            expected_class_plain = item[len(item) - 2].replace("I-", "")
            predicted_class = item[len(item) - 1].replace('\n', '')

            item_string = ''
            if not reached_annotation_beginning:
                if expected_class_plain == current_expected_class.replace("I-", ""):
                    item_string = get_item_diff_marker(expected_class, predicted_class)
                    if expected_class.startswith("I-"):
                        reached_annotation_beginning = True

            error_case_tokens.insert(0, [item[0] + item_string, expected_class, predicted_class.replace('\n', '')])

    return error_case_tokens


def append_tokens_after(error_case_tokens, example, i, tokens_after):
    data_length = len(example)
    if i + 1 < data_length:
        tokens_after = tokens_after if data_length - i > tokens_after else data_length - i - 1
        current_item = example[i]
        current_expected_class = current_item[len(current_item) - 2]

        for x in range(i + 1, i + 1 + tokens_after):
            item = example[x]
            if len(item) <= 1:
                break

            expected_class = item[len(item) - 2]
            expected_class_plain = item[len(item) - 2].replace("I-", "")
            predicted_class = item[len(item) - 1].replace('\n', '')
            item_string = ''
            if expected_class_plain == current_expected_class.replace("I-", ""):
                if not expected_class.startswith("I-"):
                    item_string = get_item_diff_marker(expected_class, predicted_class)

            error_case_tokens.append([item[0] + item_string, expected_class, predicted_class])

    return error_case_tokens


def get_item_diff_marker(expected_class, predicted_class):
    item_string = ""
    if expected_class == predicted_class:
        if expected_class == "<other>":
            item_string = ""
        else:
            item_string = "<=>"
    else:  # expected_class != predicted_class:
        if expected_class == "<other>":
            item_string = "<+>"
        else:
            # predicted class is <other> (recall issue) or other classes (precision issue)
            if predicted_class == "<other>":
                item_string = "<-r>"
            else:
                item_string = "<-p>"

    return item_string


def count_discrepancies(cases):
    wrong_prediction_suffix_precision = '<-p>'
    wrong_prediction_suffix_recall = '<-r>'
    possible_wrong_annotation_suffix = '<+>'
    # correct_annotation_suffix = '<=>'

    allowed_suffixes = [wrong_prediction_suffix_recall, wrong_prediction_suffix_precision,
                        possible_wrong_annotation_suffix]

    discrepancies_counter_by_label = {

    }

    for item in cases:
        label = item['expected'].replace("I-", "")
        if label not in discrepancies_counter_by_label:
            discrepancies_counter_by_label[label] = {}

        local_counter = discrepancies_counter_by_label[label]
        tokens_collector = []
        item_suffix = ""
        for token in item['tokens']:
            matching = re.match(r"^.+(<[+-=][rp]?>)$", token[0])
            if matching and len(matching.groups()) > 0:
                suffix = matching.group(1).strip()
                if item_suffix == "":
                    item_suffix = suffix
                elif item_suffix != matching.group(1).strip():
                    print("Suffix has changed. Ignoring")

                if item_suffix not in allowed_suffixes:
                    continue
                plain_token = token[0].replace(suffix, '')
                tokens_collector.append(plain_token)
                if item_suffix not in local_counter:
                    local_counter[suffix] = {}

            elif len(tokens_collector) > 0:
                string_tokens = tokens_to_string(tokens_collector)
                if string_tokens in local_counter[item_suffix]:
                    local_counter[item_suffix][string_tokens] += 1
                else:
                    local_counter[item_suffix][string_tokens] = 1

                tokens_collector = []

                # if plain_token in local_counter[suffix]:
                #     local_counter[suffix][plain_token].append(item[1])
                # else:
                #     local_counter[suffix][plain_token] = [item[1]]

    return discrepancies_counter_by_label


def tokens_to_string(tokens_collector):
    output = "".join(
        [x if i < len(tokens_collector) - 1 and tokens_collector[i + 1] in string.punctuation else x + " "
         for i, x in enumerate(tokens_collector)])

    return output.strip()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description="Analysis n-fold cross-validation / holdout evaluation results")

    parser.add_argument("--input", help="Input output file produced by grobid", required=True, type=Path)
    parser.add_argument("--output", help="Output directory", required=False, default=None, type=Path)
    parser.add_argument("--force", action="store_true", default=False,
                        help="Force rewrite the output directory if exists.")
    # parser.add_argument("--recursive", action="store_true", default=False,
    #                     help="Process input directory recursively. If input is a file, this parameter is ignored.")
    # parser.add_argument("--format", default='csv', choices=['tsv', 'csv'],
    #                     help="Output format.")
    # parser.add_argument("--filter", default='all', choices=['all', 'oa', 'non-oa'],
    #                     help='Extract data from a certain type of licenced documents')

    args = parser.parse_args()
    input_file = args.input
    output_dir = args.output
    force = args.force

    if not os.path.exists(input_file):
        print("The file", input_file, "does not exists. Exiting.")
        sys.exit(-1)

    if not os.path.isfile(str(input_file)):
        print("The file", input_file, "does is not a file. Exiting.")
        sys.exit(-1)

    data = extract_log(input_file)
    error_cases = extract_error_cases(data)
    grouped_cases = {}

    for case in error_cases:
        expected_label = case['expected'].replace("I-", "")
        tokens = case['tokens']
        text = ""

        for t in tokens:
            text += t[0] + " "

        if expected_label not in grouped_cases.keys():
            grouped_cases[expected_label] = []
        grouped_cases[expected_label].append(text)

    print('==========')

    ## Documentaton
    print("\n== Description ==")
    print("<=> -> the model predicts correctly")
    print("<+> -> the model recognise an entity that wasn't expected - could be a discrepancy in annotation")
    print("<-p> -> the model wrongly recognise an entity (precision) ")
    print("<-r> -> the model misses an entity (recall)")

    # class specific mismatches
    # material_keywords = ['crystal', 'crystals', 'doped', 'film', 'films', 'powder', 'bulk', 'pure', 'underdoped']
    # material_discrepancies, wrong_prediction_counter_precision, wrong_prediction_counter_recall, wrong_annotation_counter = count_discrepancies_near_annotations(
    #     grouped_cases['<material>'], material_keywords)
    # print("Material discrepancies ", material_discrepancies, "on ", len(grouped_cases['<material>']))
    # print("- Wrong predictions (precision)", wrong_prediction_counter_precision)
    # print("- Wrong predictions (recall)", wrong_prediction_counter_recall)
    # print("- Wrong annotations", wrong_annotation_counter)
    #
    # me_method_keywords = ['measurement', 'measurements', 'mea- surement', 'mea- surements', 'ac', 'dc']
    # me_method_discrepancies, wrong_prediction_counter_precision, wrong_prediction_counter_recall, wrong_annotation_counter = count_discrepancies_near_annotations(
    #     grouped_cases['<me_method>'], me_method_keywords)
    # print("Me_methods discrepancies ", me_method_discrepancies, "on ", len(grouped_cases['<me_method>']))
    # print("- Wrong predictions (precision) ", wrong_prediction_counter_precision)
    # print("- Wrong predictions (recall) ", wrong_prediction_counter_recall)
    # print("- Wrong annotations", wrong_annotation_counter)

    discrepancies = count_discrepancies(error_cases)
    # Automatic detection of mismatches
    if output_dir is None or (output_dir is not None and not os.path.isdir(output_dir)):
        print("\n** The output directory is not defined, invalid or not-a-directory: "
              "the script will only print the summary.**")
        print("\n== Summary ==")
        for label in discrepancies.keys():
            print(" \n == ", label)
            for suffix in discrepancies[label]:
                print(" === ", suffix, "count: ", len(discrepancies[label][suffix]))

        sys.exit(-1)

    print("\n== Summary ==")
    sorted_discrepancies = OrderedDict()
    root = os.path.join(output_dir, "detailed_data")
    if os.path.exists(root):
        if not force:
            print("The directory ", root,
                  "exists. To override, just run using --force or remove the directory before running the script.")
            sys.exit(-1)
        else:
            print("Removing output directory. ")
            shutil.rmtree(root)
            os.mkdir(root)
    else:
        os.mkdir(root)

    for label in discrepancies.keys():
        print(" \n == ", label)
        label_dir = os.path.join(root, label)
        os.mkdir(label_dir)
        sorted_discrepancies[label] = OrderedDict()
        for suffix in discrepancies[label]:
            suffix_dir = os.path.join(label_dir, suffix)
            os.mkdir(suffix_dir)
            print(" === ", suffix, "count: ", len(discrepancies[label][suffix]))
            sorted_items = sorted(discrepancies[label][suffix].items(), key=lambda item: item[1], reverse=True)
            for idx, sorted_item in enumerate(sorted_items[:10]):
                filename = os.path.join(suffix_dir,
                                        "item_" + str(sorted_item[0].replace(" ", "_").replace("/", "__")) + "_" + str(
                                            idx) + ".txt")
                with open(filename, 'w') as f:
                    f.write(" ==== " + str(sorted_item[0]) + " count: " + str(len(str(sorted_item[1]))))
                    f.write("\n")
                    for error_case in str(sorted_item[1]):
                        for line in error_case:
                            f.write(", ".join(line) + "\n")
                        f.write("\n")

    for i, label in enumerate(grouped_cases.keys()):
        label_dir = os.path.join(root, label)
        filename = os.path.join(label_dir, "summary.txt")
        with open(filename, 'w') as f:
            for case in grouped_cases[label]:
                f.write(case + "\n")

            # sorted_discrepancies[label][suffix] = sorted_items[:10]
