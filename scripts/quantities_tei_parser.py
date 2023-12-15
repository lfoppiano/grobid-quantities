import re
from collections import OrderedDict
from pathlib import Path
from typing import List

from bs4 import BeautifulSoup, Tag, NavigableString
from supermat.supermat_tei_parser import tokenise

ENTITY_TYPES = ['value', 'interval', 'range', 'list']

def process_file_to_json(input_file_path):
    with open(input_file_path, encoding='utf-8') as fp:
        doc = fp.read()

    mod_tags = re.finditer(r'(</\w+>) ', doc)
    for mod in mod_tags:
        doc = doc.replace(mod.group(), ' ' + mod.group(1))
    soup = BeautifulSoup(doc, 'xml')

    output_document = OrderedDict()
    output_document['doc_key'] = Path(str(input_file_path)).name
    output_document['dataset'] = 'Quantities'
    output_document['lang'] = 'en'

    output_document['level'] = 'paragraph'
    paragraph_nodes = get_nodes(soup)
    passages, relations = process_paragraphs(paragraph_nodes)

    output_document['passages'] = passages
    output_document['relations'] = relations

    return output_document


def get_nodes(soup, verbose=False):
    children = soup.find_all("p")

    if verbose:
        print(str(children))

    return children


def process_paragraphs(paragraph_list: list) -> [List, List]:
    """
    Process XML with <p> and <s> as sentences.

    Return two list passage (sentence or paragraph,spans and link) and relations (links at document-level)
    """
    token_offset_sentence = 0
    ient = 1

    passages = []
    relations = []

    i = 0
    for paragraph_id, paragraph in enumerate(paragraph_list):
        passage = OrderedDict()

        j = 0
        offset = 0
        tokens = []
        text_paragraph = ''
        spans = []

        passage['text'] = text_paragraph
        passage['tokens'] = tokens
        passage['type'] = 'paragraph'
        passage['spans'] = spans
        passage['id'] = paragraph_id

        for idx, item in enumerate(paragraph.contents):
            if type(item) is NavigableString:
                local_text = str(item).replace("\n", " ")
                # We preserve spaces that are in the middle
                if idx == 0 or idx == len(paragraph.contents) - 1:
                    local_text = local_text.strip()
                text_paragraph += local_text
                token_list = tokenise(local_text)
                tokens.extend(token_list)
                token_offset_sentence += len(token_list)
                offset += len(local_text)
            elif type(item) is Tag and item.name == 'measure' and 'type' in item.attrs and item.attrs['type'] in ENTITY_TYPES:
                local_text = item.text
                text_paragraph += local_text
                span = OrderedDict()
                front_offset = 0
                if local_text.startswith(" "):
                    front_offset = len(local_text) - len(local_text.lstrip(" "))

                span['text'] = local_text.strip(" ")
                span['offset_start'] = offset + front_offset
                span['offset_end'] = offset + len(span['text']) + front_offset
                spans.append(span)

                offset += len(local_text)

                assert text_paragraph[span['offset_start']:span['offset_end']] == span['text']

                if 'type' not in item.attrs:
                    raise Exception("RS without type is invalid. Stopping")
                token_list = tokenise(local_text)
                tokens.extend(token_list)

                entity_class = item.attrs['type']
                span['type'] = entity_class

                span['token_start'] = token_offset_sentence
                span['token_end'] = token_offset_sentence + len(token_list) - 1

                j += 1

            ient += 1  # entity No.

        passage['text'] = text_paragraph
        passages.append(passage)
        i += 1
    return passages, relations
