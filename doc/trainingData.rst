..  _training_data:

Training data
=============

As the rest of GROBID, the training data is encoded following the `TEI P5 <http://www.tei-c.org/Guidelines/P5>`_.
See :doc:`guidelines` for detailed explanations and examples.

Generation of training data
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Training data generation works the same as in GROBID, with executable name ``createTrainingQuantities``,
for example:
::
   java -jar build/libs/grobid-quantities-{version}-onejar.jar trainingGeneration -dIn ~/grobid/grobid-quantities/src/test/resources/ -dOut ~/test/


Help can be invoked with
::
   java -jar build/libs/grobid-quantities-{version}-onejar.jar trainingGeneration --help


The input directory can contain PDF (.pdf, scientific articles only), XML/TEI (.xml or .tei,
for patents and scientific articles) and text files (.txt).

