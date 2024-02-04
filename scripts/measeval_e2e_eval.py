import argparse
import csv
import math
import os
import random
from pathlib import Path

import dotenv
from textdistance import RatcliffObershelp

dotenv.load_dotenv(override=True)

from grobid_quantities.quantities import QuantitiesAPI
from tqdm import tqdm


def print_markdown(result_table):
    # Write table headers
    markdown = "|" + "|".join(result_table[0]) + "|" + "\n"
    markdown += "|" + "|".join(["---"] * len(result_table[0])) + "|" + "\n"

    # Write table rows
    for row in result_table[1:]:
        formatted_row = [f"{cell * 100:.2f}" if isinstance(cell, (int, float)) else str(cell) for cell in row]
        markdown += "|" + "|".join(formatted_row) + "|" + "\n"

    return markdown


def calculate_metrics(fn, fp, tp):
    precision_micro_avg = len(tp) / (len(tp) + len(fp))
    recall_micro_avg = len(tp) / (len(tp) + len(fn))
    f1_score_micro_avg = 2 * (precision_micro_avg * recall_micro_avg) / (precision_micro_avg + recall_micro_avg)

    return precision_micro_avg, recall_micro_avg, f1_score_micro_avg


def match(expected, predicted, matching_type, matching_threshold=None):
    expected = "" if expected is None else expected
    predicted = "" if predicted is None else predicted
    if matching_type == "strict":
        return str.lower(expected) == str.lower(predicted)
    elif matching_type == "soft" and matching_threshold is not None:
        return RatcliffObershelp().normalized_similarity(str.lower(predicted),
                                                         str.lower(expected)) >= matching_threshold
    else:
        print("Invalid matching type:", matching_type, ". Please check how to use it, --help")
        return 0


def get_matches(expected_entities, predicted_entities, matching_type="soft", matching_threshold=0.8) -> (
    list, list, list):
    tp = []
    fp = []
    fn = []

    skip_predicted_idx = []
    skip_expected_idx = []
    for idxp, predicted_entity_ in enumerate(predicted_entities):
        if idxp in skip_predicted_idx:
            continue
        predicted_entity = predicted_entity_.strip()
        is_match = False
        for idxe, expected_entity_ in enumerate(expected_entities):
            expected_entity = expected_entity_.strip()

            if idxe in skip_expected_idx:
                continue

            if match(predicted_entity, expected_entity, matching_type, matching_threshold):
                tp.append(predicted_entity)

                skip_predicted_idx.append(idxp)
                skip_expected_idx.append(idxe)
                is_match = True
                break

        if is_match is False:
            fp.append(predicted_entity)

    # fn, entities I did not predict that are expected
    for idxe, expected_entity in enumerate(expected_entities):
        if idxe not in skip_expected_idx:
            fn.append(expected_entity)

    assert "FP+TP != nb of predicted", len(tp) + len(fp) == len(predicted_entities)
    assert "FP+FN != nb of expected", len(tp) + len(fn) == len(expected_entities)

    return tp, fp, fn


def get_parsed_value_type(quantity):
    if 'parsedValue' in quantity and 'structure' in quantity['parsedValue']:
        return quantity['parsedValue']['structure']['type']


def has_space_between_value_and_unit(quantity):
    return quantity['offsetEnd'] < quantity['rawUnit']['offsetStart']


def parse_measurements_output(result):
    measurements_output = []

    for measurement in result['measurements']:
        type = measurement['type']
        measurement_output_object = {}
        quantity_type = None
        has_unit = False
        parsed_value_type = None

        if 'quantified' in measurement:
            if 'normalizedName' in measurement['quantified']:
                quantified_substance = measurement['quantified']['normalizedName']
                measurement_output_object["quantified_substance"] = quantified_substance

        if 'measurementOffsets' in measurement:
            measurement_output_object["offset_start"] = measurement["measurementOffsets"]['start']
            measurement_output_object["offset_end"] = measurement["measurementOffsets"]['end']
        else:
            # If there are no offsets we skip the measurement
            continue

        # if 'measurementRaw' in measurement:
        #     measurement_output_object['raw_value'] = measurement['measurementRaw']

        if type == 'value':
            quantity = measurement['quantity']

            parsed_value = get_parsed(quantity)
            if parsed_value:
                measurement_output_object['parsed'] = parsed_value

            normalized_value = get_normalized(quantity)
            if normalized_value:
                measurement_output_object['normalized'] = normalized_value

            raw_value = get_raw(quantity)
            if raw_value:
                measurement_output_object['raw'] = raw_value

            if 'type' in quantity:
                quantity_type = quantity['type']

            if 'rawUnit' in quantity:
                has_unit = True

            parsed_value_type = get_parsed_value_type(quantity)

        elif type == 'interval':
            if 'quantityMost' in measurement:
                quantityMost = measurement['quantityMost']
                if 'type' in quantityMost:
                    quantity_type = quantityMost['type']

                if 'rawUnit' in quantityMost:
                    has_unit = True

                parsed_value_type = get_parsed_value_type(quantityMost)

            if 'quantityLeast' in measurement:
                quantityLeast = measurement['quantityLeast']

                if 'type' in quantityLeast:
                    quantity_type = quantityLeast['type']

                if 'rawUnit' in quantityLeast:
                    has_unit = True

                parsed_value_type = get_parsed_value_type(quantityLeast)

        elif type == 'listc':
            quantities = measurement['quantities']

            if 'type' in quantities[0]:
                quantity_type = quantities[0]['type']

            if 'rawUnit' in quantities[0]:
                has_unit = True

            parsed_value_type = get_parsed_value_type(quantities[0])

        if quantity_type is not None or has_unit:
            measurement_output_object['type'] = quantity_type

        if parsed_value_type is None or parsed_value_type not in ['ALPHABETIC', 'TIME']:
            measurements_output.append(measurement_output_object)

    return measurements_output


def get_parsed(quantity):
    parsed_value = parsed_unit = None
    if 'parsedValue' in quantity and 'parsed' in quantity['parsedValue']:
        parsed_value = quantity['parsedValue']['parsed']
    if 'parsedUnit' in quantity and 'name' in quantity['parsedUnit']:
        parsed_unit = quantity['parsedUnit']['name']

    if parsed_value and parsed_unit:
        if has_space_between_value_and_unit(quantity):
            return str(parsed_value) + str(parsed_unit)
        else:
            return str(parsed_value) + " " + str(parsed_unit)


def get_normalized(quantity):
    normalized_value = normalized_unit = None
    if 'normalizedQuantity' in quantity:
        normalized_value = quantity['normalizedQuantity']
    if 'normalizedUnit' in quantity and 'name' in quantity['normalizedUnit']:
        normalized_unit = quantity['normalizedUnit']['name']

    if normalized_value and normalized_unit:
        if has_space_between_value_and_unit(quantity):
            return str(normalized_value) + " " + str(normalized_unit)
        else:
            return str(normalized_value) + str(normalized_unit)


def get_raw(quantity):
    raw_value = raw_unit = None
    if 'rawValue' in quantity:
        raw_value = quantity['rawValue']
    if 'rawUnit' in quantity and 'name' in quantity['rawUnit']:
        raw_unit = quantity['rawUnit']['name']

    if raw_value and raw_unit:
        if has_space_between_value_and_unit(quantity):
            return str(raw_value) + " " + str(raw_unit)
        else:
            return str(raw_value) + str(raw_unit)


def extract_quantities(client, texts):
    output_data = []

    for idx, example in tqdm(enumerate(texts), desc="extract quantities"):
        status, result = client.process_text(example.strip())

        if status != 200:
            result = {}

        spans = []

        if 'measurements' in result:
            found_measurements = parse_measurements_output(result)

            for m in found_measurements:
                item = {
                    "text": example[m['offset_start']:m['offset_end']],
                    'offset_start': m['offset_start'],
                    'offset_end': m['offset_end']
                }

                if 'raw' in m and m['raw'] != item['text']:
                    item['text'] = m['raw']

                if 'quantified_substance' in m:
                    item['quantified'] = m['quantified_substance']

                if 'type' in m:
                    item["unit_type"] = m['type']

                # if 'raw_value' in m:
                #     item['raw_value'] = m['raw_value']

                spans.append(item)

        data_record = {
            "text": example,
            "entities": spans
        }

        output_data.append(data_record)

    return output_data


def write_output(data, output_path, format="tsv"):
    delimiter = '\t' if format == 'tsv' else ','
    fw = csv.writer(open(output_path, encoding='utf-8', mode='w'), delimiter=delimiter, quotechar='"',
                    quoting=csv.QUOTE_ALL)
    fw.writerows(data)


def format_as_csv(output_data, output_data_list, type):
    key = "property" if type == "quantities" else "material"
    data_as_csv = []
    for idx, data in enumerate(output_data):
        related_list = output_data_list[idx][key] if key in output_data_list[idx].keys() else []
        data_as_csv.extend([[data['id'], data['filename'], data['passage_id'], property] for property in related_list])

    return data_as_csv


def compute_doc_ids(textpaths) -> (list, list):
    docIds = []
    textset = {}
    for fileset in textpaths:
        for fn in os.listdir(fileset):
            with open(fileset + fn) as textfile:
                text = textfile.read()  # .splitlines()
                textset[fn[:-4]] = text
                docIds.append(fn[:-4])
    random.seed(42)
    random.shuffle(docIds)

    return textset, docIds


def prepare_data(trainpaths, docIds, split):
    train_split_len = math.ceil((len(docIds) * (split / 100)))
    trainIds = docIds[:train_split_len]

    typemap = {
        "Quantity": "QUANT",
        "MeasuredEntity": "ME",
        "MeasuredProperty": "MP",
        "Qualifier": "QUAL"
    }
    traindata = []
    testdata = []

    for fileset in trainpaths:
        for fn in os.listdir(fileset):
            entities = []
            with open(fileset + fn) as annotfile:
                text = textset[fn[:-4]]
                next(annotfile)
                annots = annotfile.read().splitlines()
                for a in annots:
                    annot = a.split("\t")
                    atype = typemap[annot[2]]
                    start = int(annot[3])
                    stop = int(annot[4])
                    ent_text = annot[6]
                    # This is where we toss out the overlaps:
                    overlap = False
                    for ent in entities:
                        if (
                            (ent['offset_start'] <= start <= ent['offset_end']) or
                            (ent['offset_start'] <= stop <= ent['offset_end']) or
                            (start <= ent['offset_start'] <= stop) or
                            (start <= ent['offset_end'] <= stop)
                        ):
                            overlap = True
                    if overlap is False:
                        entities.append({"offset_start": start, "offset_end": stop, "type": atype, "text": ent_text})

                if fn[:-4] in trainIds:
                    traindata.append({"text": text, "entities": entities})
                else:
                    testdata.append({"text": text, "entities": entities})

    return traindata, testdata


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description="End-to-end evaluation for Grobid-quantities with the MeasEval dataset.")

    parser.add_argument("--input", help="MeasEval dataset directory", required=True)
    parser.add_argument("--config",
                        help="Configuration file",
                        default="resources/config/config.yaml",
                        required=False)

    args = parser.parse_args()

    input = args.input
    config_file = args.config

    input_path = Path(input)

    trainpaths = [
        os.path.join(input_path, "trial/tsv/"),
        os.path.join(input_path, "train/tsv/")
    ]
    textpaths = [
        os.path.join(input_path, "trial/txt/"),
        os.path.join(input_path, "train/txt/")
    ]

    textset, docIds = compute_doc_ids(textpaths)

    train_data, test_data = prepare_data(trainpaths, docIds, 70)
    all_data = train_data + test_data

    quantities_client = QuantitiesAPI(os.environ['QUANTITIES_URL'], check_server=True)

    texts = [data['text'] for data in all_data]

    output_data_quantities = extract_quantities(quantities_client, texts)
    predicted_quant_list = [[entity['text'] for entity in example['entities']] for example in
                            output_data_quantities]

    predicted_me_list = [list(filter(lambda e: e != "",
                              [entity['quantified'] if 'quantified' in entity else "" for entity in
                               example['entities']])) for example in
                         output_data_quantities]

    expected_quant_list = [
        [example['text'] for example in filter(lambda x: x['type'] == "QUANT", examples_in_text['entities'])] for
        examples_in_text in all_data]

    expected_me_list = [
        [example['text'] for example in filter(lambda x: x['type'] == "ME", examples_in_text['entities'])] for
        examples_in_text in all_data]

    result_table = [["Type", "Matching method", "Precision", "Recall", "F1-score", "Support"]]

    for mt in ["strict", "soft"]:

        tp_all = []
        fp_all = []
        fn_all = []

        predicted_count = 0

        for idx, predicted_in_text in enumerate(predicted_quant_list):
            predicted_count += len(predicted_in_text)
            expected_in_text = expected_quant_list[idx]
            tp, fp, fn = get_matches(
                expected_in_text,
                predicted_in_text,
                mt
            )
            tp_all += tp
            fp_all += fp
            fn_all += fn

        precision_micro_avg, recall_micro_avg, f1_score_micro_avg = calculate_metrics(fn_all, fp_all, tp_all)
        result_table.append(
            ["Quantities", mt, precision_micro_avg, recall_micro_avg, f1_score_micro_avg, str(predicted_count)])

    for mt in ["strict", "soft"]:

        tp_all = []
        fp_all = []
        fn_all = []

        predicted_count = 0

        for idx, predicted_in_text in enumerate(predicted_me_list):
            predicted_count += len(predicted_in_text)
            expected_in_text = expected_me_list[idx]
            tp, fp, fn = get_matches(
                expected_in_text,
                predicted_in_text,
                mt
            )
            tp_all += tp
            fp_all += fp
            fn_all += fn

        precision_micro_avg, recall_micro_avg, f1_score_micro_avg = calculate_metrics(fn_all, fp_all, tp_all)
        result_table.append(
            ["Quantified substance", mt, precision_micro_avg, recall_micro_avg, f1_score_micro_avg,
             str(predicted_count)])

    print("\n")
    print(print_markdown(result_table))
