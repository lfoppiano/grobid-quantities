.. _Python client GitHub page: https://gitlab.nims.go.jp/a017873/superconductors-documentation/issues

.. topic:: Getting started, build, install

Getting started
===============

Grobid-quantities requires *JDK 1.8 or greater* and Grobid to be installed.

Install and build
~~~~~~~~~~~~~~~~~

First install the latest development version of GROBID as explained by the `documentation <http://grobid.readthedocs.org>`_.

Grobid-quantities root directory needs to be placed as sibling sub-project inside Grobid directory:
::

   cp -r grobid-quantities grobid/

The easier is to clone directly within the Grobid directory.

Then, build everything with:
::

   cd PATH-TO-GROBID/grobid-quantities/

   ./gradlew copyModels
   ./gradlew clean install


You should have the directories of the models ``quantities``, ``units`` and ``values`` inside ``../grobid-home/models``

Run some test:
::

   cd PATH-TO-GROBID/grobid-quantities

   ./gradlew test


Start and use the service
~~~~~~~~~~~~~~~~~~~~~~~~~

Grobid-quantities can be run with the following command:
::

  java -jar build/libs/grobid-quantities-{version}-onejar.jar server resources/config/config.yml


There is a GUI interface demo accessible at ``http://localhost:8060``, and a REST API, reachable under ``http://localhost:8060/service`` and documented in the :ref:`rest_api`

To test the API, is possible to run a simple text using ``curl``:

::

  curl -X POST -F "text=I've lost two minutes." localhost:8060/service/processQuantityText


**Note**: The model is designed and trained to work at *paragraph level*. The expected text input to the parser is a paragraph or a text segment of similar size, not a complete document. In case you have a long textual document, it is better either to exploit existing structures (e.g. XML/HTML ``<p>`` elements) to initially segment it into paragraphs or sentences, or to apply an automatic paragraph/sentence segmentation. Then send them separately to grobid-quantities to be processed.


Clients
~~~~~~~

To interact with the server is possible to use the python client, which support both single and multi-thread processing.
The client can be installed using pip:
::

  pip install grobid-quantities-client

more information can be found a the `Python client GitHub page`_.

