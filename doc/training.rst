..  _training_data:

.. _quantities holdout set: https://github.com/kermitt2/grobid-quantities/tree/master/resources/dataset/quantities/readme.md
.. _units holdout set: https://github.com/kermitt2/grobid-quantities/tree/master/resources/dataset/units/readme.md
.. _values holdout set: https://github.com/kermitt2/grobid-quantities/tree/master/resources/dataset/values/readme.md

Training and evaluation
=======================

As the rest of GROBID, the training data is encoded following the `TEI P5 <http://www.tei-c.org/Guidelines/P5>`_.
See :doc:`guidelines` for detailed explanations and examples.

Generation of training data
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Training data generation works the same as in GROBID, with executable name ``createTrainingQuantities``,
for example:
::
   java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar trainingGeneration -dIn ~/grobid/grobid-quantities/src/test/resources/ -dOut ~/test/ resources/config/config.yml

by default the generation of training data is performed on the specified directory.
The argument ``-r`` will process all files within the subdirectories, recursively.

Help can be invoked with
::
   java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar trainingGeneration --help


The input directory can contain PDF (.pdf, scientific articles only), XML/TEI (.xml or .tei,
for patents and scientific articles) and text files (.txt).

Training
~~~~~~~~

This section describes quickly how to run the training with grobid-quantities.
The models will be saved under ``grobid-home/models/quantities`` and ``grobid-home/models/units`` respectively, make sure those directories exist.

To run the training, assuming the current directory is the ``grobid-quantities`` directory:
::
  cd PATH-TO-GROBID/grobid/grobid-quantities

The training can be invoked using gradle or via the the `java` command using the `dropwizard` command line, which offers more options.

.. warning:: When training, if you receive a NullPointerException after loading all the data, make sure you are providing the YAML configuration file as last parameter.

Quantities CRF model
^^^^^^^^^^^^^^^^^^^^
The trainer uses all the available training data from ``resouces/dataset/quantities/corpus``.

.. warning:: We provide already both training and evaluation corpora, which should be used to evaluate. Given the small size of the corpus, we recommend to combine `corpus` and `evaluation` only for training the final model.

Via Gradle :
::
  ./gradlew train_quantities

Via command line:
::
  java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar training -m quantities -a train resources/config/config.yml

Units CRF model
^^^^^^^^^^^^^^^
The trainer uses all the available training data from ``resouces/dataset/units/corpus``. The training instance is the unit itself (a ``<unit></unit>`` entry in the XML training file)

Via Gradle :
::
  ./gradlew train_units

Via command line:
::
  java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar training -m units -a train resources/config/config.yml


Values CRF model
^^^^^^^^^^^^^^^^
The trainer uses all the available training data from ``resouces/dataset/values/corpus``. The training instance is the unit itself (a ``<value></value>`` entry in the XML training file)

Via Gradle :
::
  ./gradlew train_values

Via command line:
::
  java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar training -m values -a train resources/config/config.yml


Quantified objects CRF model
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
**This model is not yet enabled at the moment because it's still WIP**

The trainer uses all the available training data from ``resouces/dataset/quantifiedObject/corpus``. The training instance is the paragraph itself (a ``<p></p>`` entry in the XML training file)

Via Gradle :
::
  ./gradlew train_quantifiedObject

Via command line:
::
  java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar training -m quantifiedObject -a train resources/config/config.yml


.. For the moment, the default training stop criteria are used. So, the training can be stopped manually after 2000 iterations, simply do a "control-C" to stop
the training and save the model produced in the latest iteration. 1000 iterations are largely enough.

.. Otherwise, the training will continue beyond several thousand iterations before stopping.


Evaluation
~~~~~~~~~~

Grobid-quantities can be evaluated using a random ``80/20`` ratio, an ``holdout`` set, or as ``n-fold cross-validation``.

.. note:: Since 12/2022, the holdout evaluation is the default means of evaluation for all the models.

The holdout datasets information and statistics (such as out of domain entities, overlapping rate, etc.) can be found in the Readme.md under each models directory:
 - `quantities holdout set`_
 - `units holdout set`_
 - `values holdout set`_

Holdout evaluation
^^^^^^^^^^^^^^^^^^

The holdout evaluation train the model and run the evaluation against a fixed set of training data.

The training data is taken from ``resouces/dataset/MODEL_NAME/corpus`` and the evaluation data is taken from ``resouces/dataset/MODEL_NAME/evaluation``.

The command to run the holdout evauation is:
::
  java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar training -m model_name -a holdout resources/config/config.yml

80/20 evaluation
^^^^^^^^^^^^^^^^

The 80/20 evaluation uses random 80% training data in ``resouces/dataset/MODEL_NAME/corpus`` and the remaining 20% for evaluation.

The command to run the 80/20 evaluation is:
::
  java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar training -m model_name -a train_eval resources/config/config.yml


N-fold cross-validation
^^^^^^^^^^^^^^^^^^^^^^^

The N-fold cross-validation perform the training and evaluation N times, partition the training data in N sets and using each set for evaluation while training with the rest. More detailed explanation `here <https://en.wikipedia.org/wiki/Cross-validation_(statistics)>`_.
The evaluation will then give the average scores over these n models (against test set) and for the best model which will be saved.

The command to run the n-fold cross-validation with N folds is the following:
::
  java -Djava.library.path=../grobid-home/lib/{arch}/:{MY_VIRTUAL_ENV}/lib:{MY_VIRTUAL_ENV}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar training -m model_name -a nfold --fold-count N resources/config/config.yml
