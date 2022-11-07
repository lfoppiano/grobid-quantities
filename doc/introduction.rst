.. topic:: Introduction

.. _Grobid: http://github.com/kermitt2/grobid
.. _Units of measurement: http://unitsofmeasurement.github.io/


Introduction
===============

Grobid-quantities is a Java application, based on `Grobid`_ (GeneRation Of BIbliographic Data), a machine learning framework for parsing and structuring raw documents such as PDF or plain text.
Grobid-quantities is designed for large-scale processing tasks in batch or via a web REST API.

The machine learning engine architecture follows the cascade approach, where each model is specialised in the resolution of a specific task.

.. figure:: img/cascade-schema.png
   :alt: Grobid-quantities cascade schema

The models are trained using the Conditional Random Field (CRF) algorithm and Recurrent neural networks (RNN) using the bidirectional LSTM with CRF as activation layer (BidLSTM_CRF).


**quantities** are modelled using three different types:
    (a) ``atomic values`` in case of single measurements (e.g., 10 grams),
    (b) ``interval`` (e.g. ``from 3 to 5 km``) and ``range`` (``100 +- 4``  ) for continuous values, and,
    (c) ``lists`` of discrete values where the measurement unit is shared.

**units** are decomposed and restructured. Complementary information like unit system, type of measurement are attached by lookup in an internal lexicon.

**value** are parsed, supporting different representations:
    (a) numeric (``2``, ``1000``)
    (b) alphabetic (``tw``, ``thousand``),
    (c) power of 10 (``1.5 x 10^-5``)
    (d) date/time expressions

..    (d) exponential representation using the mathematical constant ``e = 2.2718``

The measurements that are identified are normalised toward the International System of Units (SI) using the java library `Units of measurement`_.

Grobid-quantities also contains a module implementing the identification of the "quantified" object/substance related to the measure. This module is still *experimental*.

The following screenshot illustrate an example of measurement that is extracted, parsed and normalised, the quantified substance, *streptomycin* is additionally recognised:

.. figure:: img/Screenshot6.png
   :alt: Grobid-quantities extraction from text

Contacts
^^^^^^^^
Contact: Patrice Lopez (<patrice.lopez@science-miner.com>), Luca Foppiano (<luca@foppiano.org>)


License
^^^^^^^
GROBID and grobid-quantities are distributed under `Apache 2.0 license <http://www.apache.org/licenses/LICENSE-2.0>`_. The documentation is distributed under `CC-0 <https://creativecommons.org/publicdomain/zero/1.0/>`_ license. The annotated data are licenced under `CC 4.0 BY <https://creativecommons.org/licenses/by/4.0/>`_.

If you contribute to grobid-quantities, you agree to share your contribution following these licenses. 

The :ref:`References` page contains citations, acknowledgement and references resources related to the project.