Training data
=============

As the rest of GROBID, the training data is encoded following the `TEI P5 <http://www.tei-c.org/Guidelines/P5>`_.
See :doc:`guidelines` for detailed explanations and examples.

Generation of training data
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Training data generation works the same as in GROBID, with executable name ``createTrainingQuantities``, for example:
::
   java -jar target/grobid-quantities-0.4.0-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties
   -dIn ~/grobid/grobid-quantities/src/test/resources/ -dOut ~/test/ -exe createTrainingQuantities

The input directory can contain PDF (.pdf, scientific articles only), XML/TEI (.xml or .tei, for patents and scientific articles) and text files (.txt).

For the unit model the training data cannot be generated automatically from PDF. The overall effort is similar to create the training data from scratch manually.

**Advanced**: There is the possibility to generate a simple unit training data file (covering mostly all the unit once, and the combiation between SI base units and prefixes). This generator uses the file lexicon file information (notation, inflections and so on, e.g. resources/en/units.json).

To generate the data:
::
  java -jar target/grobid-quantities-0.4.0-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties
  -dIn input/resources -dOut /tmp/ -exe generateTrainingUnits

The input directory should be the directory containing prefixes.txt and units.json (normally by language) (e.g. of input/resources /~/grobid-quantities/src/main/resources/en)
