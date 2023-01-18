
.. _rest_api:

Rest API Documentation
======================

This page describes the Grobid-quantities REST API.

Response description
~~~~~~~~~~~~~~~~~~~~
The response is structured following a simple schema composed two attributes: `runtime` and `measurements` representing the request duration server side (in ms) and the list of extracted measurements, respectively.


The basic JSON structure is the following
::

  {
     "runtime": "123
     "measurements": [
        {
            "type": ...
            "quantity*": ...
            "quantified": ...
            "pages": ...
        }
     ]
  }


constituted by the following components:
 - *quantity* represents the raw quantity
 - *type* describes the measurement nature, in particular it can be ``value``, ``interval`` or ``list``. Depending on it, the property related to the quantity will change according to the table below.
 - *quantified* contains the quantified object/substance in both raw and normalised expression
 - *pages* provides the list of pages when processing a PDF document


=================  ==============================  ==============================
Measuremen type       Quantity property name(s)      Object type
=================  ==============================  ==============================
value               quantity                        quantity object
interval            quantityLeast, quantityMost     quantity objects (2)
list                quantities                      list of quantity objects
=================  ==============================  ==============================

**Note**: ranges (``10+-3``) are represented directly as intervals (``7 to 13``) in JSON.

The quantity object follow the schema
::

  "quantity": {
    "type": "time",
    "rawValue": "two",
    "rawUnit": {...}
    "parsedValue": {...}
    "normalizedQuantity": 120
    "normalizedUnit": {...}
    "offsetStart": 7,
    "offsetEnd": 10
  }

which has three main objects:
 - rawValue and rawUnit contains information as they appear in input
 - parsedValue and parsedUnit contains parsed information (note than parsedUnit is ignored when the normalisation is successfully executed)
 - normalisedQuantity and normalisedUnit contains normalisation information

Process Quantities from Text
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Process text and extract and normalise measurements. The access point can be reach by:
::

  POST    /service/processQuantityText

By processing our classical example ``I've lost two minutes``:
::

    curl -X POST -F "text=I've lost two minutes." localhost:8060/service/processQuantityText 


It will returns a JSON response looking like

::

  {
    "runtime": 52,
    "measurements": [
        {
            "type": "value",
            "quantity": {
                "type": "time",
                "rawValue": "two",
                "rawUnit": {
                    "name": "minutes",
                    "type": "time",
                    "system": "non SI",
                    "offsetStart": 11,
                    "offsetEnd": 18
                },
                "parsedValue": {
                    "numeric": 2,
                    "structure": {
                        "type": "ALPHABETIC",
                        "formatted": "two"
                    },
                    "parsed": "two"
                },
                "normalizedQuantity": 120,
                "normalizedUnit": {
                    "name": "s",
                    "type": "time",
                    "system": "SI base"
                },
                "offsetStart": 7,
                "offsetEnd": 10
            }
        }
    ]
  }

Another example of a quantity of type interval looks as below:
::

  {
    "runtime": 3,
    "measurements": [
        {
            "type": "interval",
            "quantityLeast": {
                "type": "time",
                "rawValue": "1",
                "rawUnit": {
                    "name": "minutes",
                    "type": "time",
                    "system": "non SI",
                    "offsetStart": 26,
                    "offsetEnd": 33
                },
                "parsedValue": {
                    "numeric": 1,
                    "structure": {
                        "type": "NUMBER",
                        "formatted": "1"
                    },
                    "parsed": "1"
                },
                "normalizedQuantity": 60,
                "normalizedUnit": {
                    "name": "s",
                    "type": "time",
                    "system": "SI base"
                },
                "offsetStart": 18,
                "offsetEnd": 19
            },
            "quantityMost": {
                "type": "time",
                "rawValue": "2",
                "rawUnit": {
                    "name": "minutes",
                    "type": "time",
                    "system": "non SI",
                    "offsetStart": 26,
                    "offsetEnd": 33
                },
                "parsedValue": {
                    "numeric": 2,
                    "structure": {
                        "type": "NUMBER",
                        "formatted": "2"
                    },
                    "parsed": "2"
                },
                "normalizedQuantity": 120,
                "normalizedUnit": {
                    "name": "s",
                    "type": "time",
                    "system": "SI base"
                },
                "offsetStart": 24,
                "offsetEnd": 25
            }
        }
    ]
  }



Process Quantities from PDF
~~~~~~~~~~~~~~~~~~~~~~~~~~~
Process PDF and generate annotations of measurements. The results are annotations which, by containing coordinate information, can be used to annotate directly a PDF.
The access point can be reach by:
::

  POST    /service/annotateQuantityPDF

and the file can be supplied using the ``input`` FormData parameter.

For instance with a ``curl`` query:
::

  curl --form input=@./myFile.pdf localhost:8060/service/annotateQuantityPDF

The result follow the usual schema described above. For this case the resulting JSON contains the list of *pages* and their dimensions. Each measurement provides the coordinate for annotating each part of the entity on the PDF.
::

  {
    "runtime": 32186,
    "pages": [
        {
            "page_height": 792,
            "page_width": 612
        },
        [...]
    ],
    "measurements": [
        {
            "type": "value",
            "quantity": {
                "type": "time",
                "rawValue": "many",
                "rawUnit": {
                    "name": "years",
                    "type": "time",
                    "system": "non SI",
                    "offsetStart": 2730,
                    "offsetEnd": 2735
                },
                "parsedValue": {
                    "numeric": 0,
                    "structure": {
                        "type": "ALPHABETIC",
                        "formatted": "many"
                    },
                    "parsed": "many"
                },
                "normalizedQuantity": 0,
                "normalizedUnit": {
                    "name": "s",
                    "type": "time",
                    "system": "SI base"
                },
                "offsetStart": 2725,
                "offsetEnd": 2729
            },
            "boundingBoxes": [
                {
                    "p": 2,
                    "x": 169.346,
                    "y": 422.195,
                    "w": 20.9665,
                    "h": 8.341
                },
                {
                    "p": 2,
                    "x": 194.178,
                    "y": 422.195,
                    "w": 18.453750000000003,
                    "h": 8.341
                }
            ]
        },
        [..]
    ]
  }


Parse measures
~~~~~~~~~~~~~~
This function takes in input a partially structured measurement and returns the normalised version.

It can be reached by
::

  POST    /service/parseMeasure

with ``raw body`` with the following schema:
::

  {
     "from" : "10",
     "to" : "20",
     "type" : "length",
     "unit": "km"
   }



It will returns something like:
::

  {
    "runtime": 2120,
    "measurements": [
        {
            "type": "interval",
            "quantityLeast": {
                "type": "length",
                "rawValue": "10",
                "rawUnit": {
                    "name": "km"
                },
                "normalizedQuantity": 10,
                "normalizedUnit": {
                    "name": "m",
                    "type": "length",
                    "system": "SI base"
                }
            },
            "quantityMost": {
                "type": "length",
                "rawValue": "20",
                "rawUnit": {
                    "name": "km"
                },
                "normalizedQuantity": 20,
                "normalizedUnit": {
                    "name": "m",
                    "type": "length",
                    "system": "SI base"
                }
            }
        }
    ]
  }

Parse units from Text
~~~~~~~~~~~~~~~~~~~~~
This entry point is used to structure units.

It can be accessed at:
::

  POST    /service/processUnitsText

The following text ``cm^2∕W`` with a ``FormParam`` parameter ``text`` will be structured in the following products:
::

  [
    {
        "prefix": "c",
        "base": "m",
        "pow": "^",
        "rawTaggedValue": "<prefix>c</prefix><base>m</base>^<pow>2</pow>"
    },
    {
        "prefix": "",
        "base": "∕",
        "pow": "",
        "rawTaggedValue": "<base>∕</base>"
    },
    {
        "prefix": "",
        "base": "W",
        "pow": "",
        "rawTaggedValue": "<base>W</base>"
    }
  ]

Service checks
~~~~~~~~~~~~~~

You can check whether the service is up and running by opening the following URL:

- GET ``http://yourhost:8060/service/health`` will return you the result of the health check

- GET ``http://yourhost:8060/service/isalive`` will return true/false whether the service is up and running


Version
~~~~~~~

The version and the last git revision are available at the following URL
::

  GET http://yourhost:8060/service/version

as
::
   {
    "version":"0.7.2-SNAPSHOT",
    "revision":"0.7.1-29-g26a151b.dirty"
   }

The version is 0.7.2-SNAPSHOT and the revision `g26a151b` allow to know what is the last commit


Maximum parallel requests limit
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This parameter allow to limit the number of parallel requests that can be send to the service. 
It can be modified in the configuration file the item `maxParallelRequests`.  
By default, the number is set to 0, which indicate to allow a number of parallel requests not higher than the number of available CPUs. 
