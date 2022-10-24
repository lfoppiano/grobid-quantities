import re

from bs4 import BeautifulSoup, Tag, NavigableString

from .grobid_tokenizer import tokenizeSimple


def tokenise(string):
    return tokenizeSimple(string)


def get_children_list(soup, verbose=False):
    children = soup.find_all("p")

    if verbose:
        print(str(children))

    return children
