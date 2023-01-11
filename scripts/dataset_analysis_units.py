import argparse
import copy
import json
import os
from difflib import SequenceMatcher
from pathlib import Path

from bs4 import BeautifulSoup, NavigableString, Tag


def process_dir(input):
    accumulated_statistics = []
    for root, dirs, files in os.walk(input):
        for file_ in files:
            if not file_.lower().endswith(".xml"):
                continue
            abs_path = os.path.join(root, file_)
            #             print("Processing: " + str(abs_path))
            output_data = process_file(abs_path)
            accumulated_statistics.append(output_data)

    return accumulated_statistics


def process_file(input):
    with open(input, encoding='utf-8') as fp:
        doc = fp.read()

    soup = BeautifulSoup(doc, 'xml')

    entities_statistics = {}
    document_statistics = {
        'name': Path(input).name,
        'path': str(Path(input).absolute()),
        'materials': 0,
        'uniq_entities': 0,
        'entities': 0,
        'classes': 0,
        'entities_statistics': entities_statistics
    }

    root = soup.units

    for i, pTag in enumerate(root('unit')):
        document_statistics['materials'] += 1

        j = 0
        paragraphText = ''
        for item in pTag.contents:
            if type(item) == NavigableString:
                paragraphText += str(item)

            elif type(item) is Tag:
                document_statistics['entities'] += 1
                tag_content = item.text
                paragraphText += str(item.text)
                tag_name = item.name

                if tag_name not in entities_statistics:
                    entities_statistics[tag_name] = {
                        'count': 1,
                        'content_distribution': {
                            tag_content: 1
                        }
                    }
                else:
                    content_ = entities_statistics[tag_name]
                    content_['count'] += 1
                    if tag_content not in content_['content_distribution']:
                        content_['content_distribution'][tag_content] = 1
                    else:
                        content_['content_distribution'][tag_content] += 1

        #         document_statistics['tokens'] += len(tokenizeSimple(paragraphText))
        #         document_statistics['sentences'] += len(split_sentences(paragraphText))
        document_statistics['classes'] = len(set(entities_statistics.keys()))

    uniq_entities = 0
    for key in entities_statistics:
        uniq_entities += len(entities_statistics[key]['content_distribution'])

    document_statistics['uniq_entities'] = uniq_entities

    ## Cross checks

    # Verify that the sum of the content distribution corresponds to the tag distribution
    total_entities = 0
    for tag in entities_statistics:
        count = entities_statistics[tag]['count']
        sum_content_distributions = 0
        content_distribution_dict = entities_statistics[tag]['content_distribution']
        for content in content_distribution_dict:
            sum_content_distributions += content_distribution_dict[content]

        assert "Number of total entities per tag does not correspond to the sum.", count == sum_content_distributions
        total_entities += count

    assert "Number of total entities per documnent does not correspond to the sum.", total_entities == \
                                                                                     document_statistics['entities']

    return document_statistics


def group_by_with_soft_matching(input_list, threshold):
    matching = {}
    last_matching = -1

    for index_x, x in enumerate(input_list):
        unpacked = [y for x in matching for y in matching[x]]
        if x not in matching and x not in unpacked:
            matching[x] = []

            for index_y, y in enumerate(input_list[index_x + 1:]):
                if x == y:
                    continue

                if SequenceMatcher(None, x.lower(), y.lower()).ratio() > threshold:
                    matching[x].append(y)

        else:
            continue

    return matching


def aggregate(entities_statistics, threshold, skip=['other'], only=None):
    """
    Aggregate the statistics by merging content belonging to the same entity:
     - variation of expressions (e.g. cuprates, cuprate, Cuprates, ...)
     - synonyms (e.g. 111, cuprates, ...)

    :param document_statistics:
    :param threshold:
    :return: an aggregated statistics for documents
    """

    agg = {}

    for tag in entities_statistics:
        if (only is not None and tag not in only) or tag in skip:
            continue

        distribution = entities_statistics[tag]["content_distribution"]

        content_list = sorted(distribution.entities_list())
        # hash_list = []
        # for content in content_list:
        #     hash_value = content.lower().replace(" ", "")
        #     hash_list.append((hash_value, content))

        aggregated = group_by_with_soft_matching(content_list, threshold)

        agg[tag] = aggregated

        assert "Total number of element does not corresponds with the aggregated ones", len(content_list) == (
            len(agg.keys()) + len([y for x in aggregated for y in aggregated[x]]))

    return agg


def extract_csv(output_data):
    entity_statistics = output_data['entities_statistics']
    csv_rows = []
    for tag in entity_statistics:
        for content in entity_statistics[tag]['content_distribution']:
            row = [tag, content, entity_statistics[tag]['content_distribution'][content]]
            csv_rows.append(row)

    return csv_rows


def intersection(lst1, lst2):
    # Use of hybrid method
    temp = set(lst2)
    lst3 = [value for value in lst1 if value in temp]
    return lst3


def extract_inconsistencies(output_data):
    entity_statistics = output_data['entities_statistics']
    summary_content = {}
    for tag in entity_statistics:
        for content in entity_statistics[tag]['content_distribution']:
            if tag in summary_content:
                summary_content[tag].append(content)
            else:
                summary_content[tag] = [content]

    inconsistencies = []

    tags = list(summary_content.keys())
    for id1 in range(0, len(tags)):
        for id2 in range(id1 + 1, len(tags)):
            tag1 = tags[id1]
            tag2 = tags[id2]

            tag1_content = summary_content[tag1]
            tag2_content = summary_content[tag2]

            intersected_content = intersection(tag1_content, tag2_content)

            if len(intersected_content) > 0:
                for intersected_content_ in intersected_content:
                    frequency1 = entity_statistics[tag1]['content_distribution'][intersected_content_]
                    frequency2 = entity_statistics[tag2]['content_distribution'][intersected_content_]
                    intersected_tags = [(tag1, frequency1), (tag2, frequency2)]
                    inconsistencies.append([intersected_content_, tag1, frequency1, tag2, frequency2])

    return inconsistencies


def find_longest_entities(output_data, topValues=10):
    print(output_data)


def run_analysis(input):
    output_data = {}

    input_path = Path(input)
    documents_statistics = process_dir(input_path)

    aggregated_entities_statistics = {}
    output_data = {
        'path': str(Path(input_path).absolute()),
        'files': len(documents_statistics),
        'entities': 0,
        'uniq_entities': 0,
        'classes': 0,
        'materials':0,
        'entities_statistics': aggregated_entities_statistics
    }

    classes = []

    ## Summary of all articles

    for document_statistics in documents_statistics:
        # output_data['paragraphs'] += document_statistics['paragraphs']
        output_data['entities'] += document_statistics['entities']
        output_data['uniq_entities'] += document_statistics['uniq_entities']
        output_data['materials'] += document_statistics['materials']

        for tag in document_statistics['entities_statistics']:
            classes.append(tag)
            tag_statistics = document_statistics['entities_statistics'][tag]
            if tag not in aggregated_entities_statistics:
                aggregated_entities_statistics[tag] = copy.copy(tag_statistics)
            else:
                count = tag_statistics['count']
                aggregated_entities_statistics[tag]['count'] += count

                dist = tag_statistics['content_distribution']
                aggregated_distribution = aggregated_entities_statistics[tag]['content_distribution']

                for content in dist:
                    if content not in aggregated_distribution:
                        aggregated_distribution[content] = dist[content]
                    else:
                        aggregated_distribution[content] += dist[content]

        output_data['classes'] = len(set(classes))

    output_data['documents'] = documents_statistics
    # output_data['aggregated_statistics'] = aggregate(aggregated_entities_statistics, 0.90)
    # output_data["inconsistencies"] = extract_inconsistencies(output_data)

    # find_longest_entities(output_data)

    return output_data


def compute_out_domain(analysis_evaluation, analysis_corpus):
    in_domain = {}
    out_domain = {}
    for label in entities_list:
        in_domain[label] = []
        out_domain[label] = []
        label_distribution_evaluation = analysis_evaluation['entities_statistics'][label]['content_distribution']
        label_distribution_corpus = analysis_corpus['entities_statistics'][label]['content_distribution']
        for entity_value in label_distribution_evaluation.keys():
            if entity_value in label_distribution_corpus.keys():
                in_domain[label].append((entity_value, label_distribution_evaluation[entity_value]))
            else:
                out_domain[label].append((entity_value, label_distribution_evaluation[entity_value]))

    return out_domain, in_domain


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Perform analysis on the corpus and holdout set. ")
    parser.add_argument("--corpus", type=str, help="Path to the corpus in XML TEI", required=True)
    parser.add_argument("--evaluation", type=str, help="Path of the holdout sets", required=True)
    parser.add_argument("--dump-statistics", action="store_true",
                        help="Write the JSON containing all the statistics on a file (corpus_statistics.json"
                             " and evaluation_statistics.json)",
                        required=False)

    args = parser.parse_args()
    corpus_path = args.corpus
    evaluation_path = args.evaluation
    dump_statistics = args.dump_statistics

    analysis_corpus = run_analysis(corpus_path)
    analysis_evaluation = run_analysis(evaluation_path)

    if dump_statistics:
        with open("corpus_statistics.json", 'w') as csf:
            json.dump(analysis_corpus, csf, indent=4)

        with open("evaluation_statistics.json", 'w') as esf:
            json.dump(analysis_evaluation, esf, indent=4)

    ## Data analysis
    columns = ['entities', 'uniq_entities', 'classes', 'materials']
    rows_corpus = [analysis_corpus[c] for c in columns]
    rows_evaluation = [analysis_evaluation[c] for c in columns]

    markdown_table = "|set " + "|" + "|".join(columns) + "|" + "\n"
    markdown_table += (len(columns) + 1) * "|---  " + "|" + "\n"
    markdown_table += "| training " + "|" + "|".join([str(x) for x in rows_corpus]) + "|" + "\n"
    markdown_table += "| holdout " + "|" + "|".join([str(x) for x in rows_evaluation]) + "|" + "\n"
    markdown_table += "| ratio" + "|" + "|".join(["{:.2f}%".format(rows_evaluation[x] / rows_corpus[x] * 100) for x in
                                                  range(0, len(rows_evaluation))]) + "|" + "\n"

    markdown_table += "\n\n\n"

    ## Labels analysis
    entities_list = list(analysis_corpus['entities_statistics'].keys())
    entities_list.remove("other")
    markdown_table += "| set " + "|" + "|".join(entities_list) + "|" + "\n"
    markdown_table += (1 + len(entities_list)) * "|---  " + "|" + "\n"
    markdown_table += "| training " + "|" + "|".join([str(analysis_corpus['entities_statistics'][x]['count']) for x in
                                                      entities_list]) + "|" + "\n"
    markdown_table += "| holdout " + "|" + "|".join(
        [str(analysis_evaluation['entities_statistics'][x]['count']) for x in
         entities_list]) + "|" + "\n"
    markdown_table += "| ratio " + "|" + "|".join(["{:.2f}%".format(
        analysis_evaluation['entities_statistics'][x]['count'] / analysis_corpus['entities_statistics'][x][
            'count'] * 100) for x in entities_list]) + "|" + "\n"

    markdown_table += "\n\n\n"

    ## Out of domain analsysis
    markdown_table += "Out of domain" + "\n"
    markdown_table += "| label | # in domain | # in domain uniques | # out domain | # out domain unique |" + "\n"
    markdown_table += 5 * "|---  " + "|" + "\n"

    out_domain, in_domain = compute_out_domain(analysis_evaluation, analysis_corpus)

    for label in entities_list:
        markdown_table += "|" + label + "|" + str(sum(e[1] for e in in_domain[label])) + "|" + str(
            len(in_domain[label])) + "|" + str(sum(e[1] for e in out_domain[label])) + "|" + str(
            len(out_domain[label])) + "|\n"

    print(markdown_table)
