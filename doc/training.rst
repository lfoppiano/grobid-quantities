..  _training_data:

Training
========

As the rest of GROBID, the training data is encoded following the `TEI P5 <http://www.tei-c.org/Guidelines/P5>`_.
See :doc:`guidelines` for detailed explanations and examples.

Generation of training data
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Training data generation works the same as in GROBID, with executable name ``createTrainingQuantities``,
for example:
::
   java -jar build/libs/grobid-quantities-{version}-onejar.jar trainingGeneration -dIn ~/grobid/grobid-quantities/src/test/resources/ -dOut ~/test/

by default the generation of training data is performed on the specified directory. To enable recursion over all its subdirectories, use the argument ``-r true``.

Help can be invoked with
::
   java -jar build/libs/grobid-quantities-{version}-onejar.jar trainingGeneration --help


The input directory can contain PDF (.pdf, scientific articles only), XML/TEI (.xml or .tei,
for patents and scientific articles) and text files (.txt).


Training the CRF models
~~~~~~~~~~~~~~~~~~~~~~~

This section describes quickly how to run the training with grobid-quantities.
The models will be saved under ``grobid-home/models/quantities`` and ``grobid-home/models/units`` respectively, make sure those directories exist.

To run the training, assuming the current directory is the ``grobid-quantities`` directory:
::
  cd PATH-TO-GROBID/grobid/grobid-quantities

The training can be invoked using gradle directly:

Quantities CRF model
^^^^^^^^^^^^^^^^^^
This model pick up the training data from ``resouces/dataset/quantities/corpus/final`` and automatically split 80/20 for training/testing.

The training ``instance`` is currently set to paragraph (``<p></p>`` entry).

To run the training:
::
  ./gradlew train_quantities


Units CRF model
^^^^^^^^^^^^^^^
This model pick up the training data from ``resouces/dataset/units/corpus`` and automatically split 80/20 for training/testing.

The training instance is the unit itself (a ``<unit></unit>`` entry in the XML training file)

To run the training:
::
  ./gradlew train_units


Values CRF model
^^^^^^^^^^^^^^^^
This model pick up the training data from ``resouces/dataset/values/corpus`` and automatically split 80/20 for training/testing.

The training instance is the unit itself (a ``<unit></unit>`` entry in the XML training file)

To run the training:
::
  ./gradlew train_values


Quantified objects CRF model
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
**This model is not yet enabled at the moment because it's still WIP**

This model pick up the training data from ``resouces/dataset/quantifiedObject/corpus`` and automatically split 80/20 for training/testing.

The training ``instance`` is currently set to paragraph (``<p></p>`` entry).

To run the training:
::
  ./gradlew train_quantifiedObject





.. For the moment, the default training stop criteria are used. So, the training can be stopped manually after 1000 iterations, simply do a "control-C" to stop
the training and save the model produced in the latest iteration. 1000 iterations are largely enough.

.. Otherwise, the training will continue beyond several thousand iterations before stopping.
