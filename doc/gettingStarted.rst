.. topic:: Getting started, build, install

Getting started
===============

Building grobid-quantities requires *maven* and *JDK 1.8*.

Build and install
~~~~~~~~~~~~~~~~~

First install the latest development version of GROBID as explained by the `documentation <http://grobid.readthedocs.org>`_.

Copy the module quantities as sibling sub-project to grobid-core, grobid-trainer, etc.:
::
   cp -r grobid-quantities grobid/

Try compiling everything with:
::
   cd PATH-TO-GROBID/grobid/

   mvn -Dmaven.test.skip=true clean install

Run some test:
::
   cd PATH-TO-GROBID/grobid/grobid-quantities

   mvn compile test

**The models have to be trained before running the tests!**

Training
~~~~~~~~

The models will be saved under ``grobid-home/models/quantities`` and ``grobid-home/models/units`` respectively, make sure those directories exist.

For training the quantity model:
::
  cd PATH-TO-GROBID/grobid/grobid-quantities

  mvn generate-resources -Ptrain_quantities

For training the unit model:
::
  mvn generate-resources -Ptrain_units

For the moment, the default training stop criteria are used. So, the training can be stopped manually after 1000 iterations, simply do a "control-C" to stop
the training and save the model produced in the latest iteration. 1000 iterations are largely enough.

Otherwise, the training will continue beyond several thousand iterations before stopping.


Start the service
~~~~~~~~~~~~~~~~~

Grobid quantities can be run as a service using jetty:
::
  mvn -Dmaven.test.skip=true jetty:run-war

Demo/console web app is then accessible at ``http://localhost:8060``

Using ``curl`` POST/GET requests:
::
  curl -X POST -d "text=I've lost one minute." localhost:8060/processQuantityText

  curl -GET --data-urlencode "text=I've lost one minute." localhost:8060/processQuantityText

Note that the model is designed and trained to work at *paragraph level*.
It means that, for the moment, the expected input to the parser is a paragraph or a text segment of similar size, not a complete document.
In case you have a long textual document, it is better either to exploit existing structures (e.g. XML/HTML elements) to segment it
initially into paragraphs or sentences, or to apply an automatic paragraph/sentence segmentation, and then send separately to
grobid-quantities the equivalent of a paragraph-size texts to be processed.
