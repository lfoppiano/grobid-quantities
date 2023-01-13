"""
    Original from https://github.com/ourreach/software-mention
    https://github.com/ourresearch/software-mentions/blob/master/scripts/createHoldoutSet.py

    Use the full softcite dataset (one TEI entry per document, including documents without
    annotation) to create an Holdout set satisfying the following constraints:

    1. same distribute of document with at least one annotation as the full corpus
    2. similar distribution of the number of annotations per document as the full corpus
    3. same distribution in fields (Biomedicine / Economics)

    For ensure point 2, we use a stratified sampling with 5 strata, using scikit-learn.

    We produce:
    - partition of the Softcite dataset (positive examples) into holdout set and working
      set
    - partition of the full text TEI (from GROBID, non-annotated) into holdout set and
      working set
    - the holdout set of full text documents (all paragraphs), with the annotated version
      of the paragraph including annotations
    - partition of the undersampling resource (the negative example XML file) into negative
      examples of the holdout set and negative examples of the working set
"""

import argparse
import math
import os
import shutil
from collections import OrderedDict
from pathlib import Path

from bs4 import BeautifulSoup
from sklearn.model_selection import train_test_split
from tqdm import tqdm


def build_annotation_map(document_list: list, tag_names: list):
    # for each document, indicate its number of annotations
    annotation_map = OrderedDict()

    for doc in tqdm(document_list, desc="Reading corpus"):
        local_id = doc
        with open(doc, encoding='utf-8') as fp:
            doc = fp.read()
        soup = BeautifulSoup(doc, 'xml')

        nb_entities = len(soup.find_all(tag_names))

        annotation_map[local_id] = nb_entities

    annotation_map_sorted = OrderedDict(sorted(annotation_map.items(), key=lambda x: x[1]))

    return annotation_map_sorted


def create_holdout_sets(document_list, ratio=0.2, nb_strata=6):
    annotation_map = build_annotation_map(document_list)

    # build the strata from annotation counts
    sum_annotation_counts = sum(annotation_map.values())
    size = (sum_annotation_counts // nb_strata) + 1

    print("Size:", str(size), ", Annotation counts: ", sum_annotation_counts)

    # map the doc to its "subpopulation", a category given as an int from 0 to (nb_strata -1)
    category_map = {}
    strata_rank = 0
    strata_size = 0
    for doc, annotation_count in annotation_map.items():
        category_map[doc] = strata_rank
        strata_size += annotation_count
        if strata_size >= size:
            strata_rank += 1
            strata_size = 0

    # create arrays for sampling
    all_docs = []
    all_cat = []
    for doc in category_map:
        all_docs.append(doc)
        all_cat.append(category_map[doc])

    holdout_size = math.floor(len(all_docs) * ratio)
    train_size = len(all_docs) - holdout_size

    # this will perform the stratified sampling following the "number of annotation" subpopulations 
    docs_training, docs_holdout = train_test_split(all_docs, test_size=holdout_size, train_size=train_size,
                                                   shuffle=True,
                                                   stratify=all_cat)

    return docs_training, docs_holdout


def build_resources(docs_training, docs_holdout, output_dir):
    print("Holdout set:", len(docs_holdout), "documents")
    print("Training set:", len(docs_training), "documents")

    if not os.path.exists(output_dir):
        os.mkdir(output_dir)

    holdout_path = os.path.join(output_dir, "holdout")
    if not os.path.exists(holdout_path):
        os.mkdir(holdout_path)

    training_path = os.path.join(output_dir, "training")
    if not os.path.exists(training_path):
        os.mkdir(training_path)

    for xml_input_path in tqdm(docs_holdout, desc="Building holdout corpus"):
        xml_output_path = os.path.join(holdout_path, Path(xml_input_path).name)
        # feature_input_path = xml_input_path.replace(".tei.xml", ".features.txt")
        # feature_output_path = os.path.join(holdout_path, Path(feature_input_path).name)

        shutil.copy(xml_input_path, xml_output_path)
        # shutil.copy(feature_input_path, feature_output_path)

    for xml_input_path in tqdm(docs_training, desc="Building training corpus"):
        xml_output_path = os.path.join(training_path, Path(xml_input_path).name)
        # feature_input_path = xml_input_path.replace(".tei.xml", ".features.txt")
        # feature_output_path = os.path.join(training_path, Path(feature_input_path).name)

        shutil.copy(xml_input_path, xml_output_path)
        # shutil.copy(feature_input_path, feature_output_path)





if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Create the holdout set from the full TEI SuperMat corpus")
    parser.add_argument("--corpus", type=str, help="Path to the full corpus file", required=True)
    parser.add_argument("--output-dir", type=str,
                        help="Path of the output directory where to copy the train and holdout sets", required=True)
    parser.add_argument("--ratio", type=float,
                        help="Proportion of documents tobe assigned to the holdout set.", default=0.2)
    parser.add_argument("--strata", type=int,
                        help="Number of stratification layers.", default=5)
    args = parser.parse_args()
    corpus_path = args.corpus
    ratio = args.ratio
    output_dir = args.output_dir
    nb_strata = args.strata

    documents_list = []
    for root, dirs, files in os.walk(corpus_path):
        for file_ in files:
            if not file_.lower().endswith(".xml"):
                continue

            abs_path = os.path.join(root, file_)
            documents_list.append(abs_path)

    docs_train, docs_holdout = create_holdout_sets(documents_list, ratio, nb_strata)

    build_resources(docs_train, docs_holdout, output_dir)
