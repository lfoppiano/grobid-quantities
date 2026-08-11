# Rest API Documentation

This page describes the Grobid-quantities REST API.

## Response description

The response is structured following a simple schema composed two attributes: `runtime` and `measurements` representing
the request duration server side (in ms) and the list of extracted measurements, respectively.

The basic JSON structure is the following :

``` json
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
```

constituted by the following components:

- *quantity* represents the raw quantity
- *type* describes the measurement nature, in particular it can be `value`, `interval` or `list`. Depending on it, the
  property related to the quantity will change according to the table below.
- *quantified* contains the quantified object/substance in both raw and normalised expression
- *pages* provides the list of pages when processing a PDF document

| Measurement type | Quantity property name(s)   | Object type              |
|------------------|-----------------------------|--------------------------|
| value            | quantity                    | quantity object          |
| interval         | quantityLeast, quantityMost | quantity objects (2)     |
| list             | quantities                  | list of quantity objects |

**Note**: ranges (`10+-3`) are represented directly as intervals (`7 to 13`) in JSON.

The quantity object follow the schema :

``` json
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
```

which has three main objects:

- rawValue and rawUnit contains information as they appear in input
- parsedValue and parsedUnit contains parsed information (note than parsedUnit is ignored when the normalisation is successfully executed)
- normalisedQuantity and normalisedUnit contains normalisation information

## Process Quantities from Text

Process text and extract and normalise measurements. The access point
can be reach by: :


>    POST    /service/processQuantityText

By processing our classical example `I've lost two minutes`: :

```shell
    curl -X POST -F "text=I've lost two minutes." localhost:8060/service/processQuantityText 
```

It will return a JSON response looking like

``` json
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
```

Another example of a quantity of type interval looks as below: :

``` json
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
```

## Process Quantities from PDF

Process PDF and generate annotations of measurements. The results are annotations which, by containing coordinate information, can be used to annotate directly a PDF. 
The access point can be reach by:

>    POST    /service/annotateQuantityPDF

and the file can be supplied using the `input` FormData parameter.

For instance with a `curl` query: :

``` shell
    curl --form input=@./myFile.pdf localhost:8060/service/annotateQuantityPDF
```

The result follow the usual schema described above. 
For this case the resulting JSON contains the list of *pages* and their dimensions. 
Each measurement provides the coordinate for annotating each part of the entity on the PDF. 

``` json
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
```

## Parse measures

This function takes in input a partially structured measurement and returns the normalised version.

It can be reached by :

>    POST    /service/parseMeasure

with `raw body` with the following schema: :

``` json
    {
       "from" : "10",
       "to" : "20",
       "type" : "length",
       "unit": "km"
     }
```

It will returns something like: :

``` json
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
```

## Parse units from Text

This entry point is used to structure units.

It can be accessed at: :

>     POST    /service/processUnitsText

The following text `cm^2∕W` with a `FormParam` parameter `text` will be structured in the following products:

``` json
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
```

## Service checks

You can check whether the service is up and running by opening the
following URL:

- GET `http://yourhost:8060/service/health` will return you the result of the health check
- GET `http://yourhost:8060/service/isalive` will return true/false whether the service is up and running

## Version

The version and the last git revision are available at the following URL:

>    GET http://yourhost:8060/service/version

and the result is as follows: :

``` json
    {
     "version":"0.7.2-SNAPSHOT",
     "revision":"0.7.1-29-g26a151b.dirty"
    }
```

The version is 0.7.2-SNAPSHOT and the revision `g26a151b` allow to know what is the last commit

## Maximum parallel requests limit

This parameter allow to limit the number of parallel requests that can be sent to the service. 
It can be modified in the configuration file the item `maxParallelRequests`. 
By default, the number is set to 0, which indicate to allow a number of parallel requests not higher than the number of available CPUs.
A negative value removes the limit altogether.

Requests arriving while all the slots are taken are **not** rejected straight away: they are
queued until a slot frees up. Three further settings bound that queue:

| Setting                          | Default     | Meaning                                                                       |
|----------------------------------|-------------|-------------------------------------------------------------------------------|
| `requestQueueMaxSize`              | `1024`      | how many requests may wait at once; a negative value means an unbounded queue  |
| `requestQueueMaxWait`        | `0 seconds` | how long one may wait; `0` means it waits until a slot frees up, however long  |
| `requestQueueRejectStatus`  | `503`       | the status returned to a request that exceeds either bound                     |

The effective values are logged at startup:

```
Limiting parallel requests: maxParallelRequests=8, requestQueueMaxSize=1024, requestQueueMaxWait=unbounded, rejecting with 503
```

### These are not response-time limits

Neither the settings above nor the connector's `idleTimeout` bound how long the service may take
to compute a response once the request holds a slot. This is a recurring source of confusion, and
it is what issue [#159](https://github.com/lfoppiano/grobid-quantities/issues/159) reported: a
service raising `idleTimeout` to 120 seconds still saw requests fail with `503` after almost
exactly 30 seconds.

The 30 seconds came from the queue, not from the connector. The limit was then implemented with
Jetty 9's `QoSFilter`, which suspended over-limit requests using the servlet *default* async
timeout — 30 seconds — and rejected them with `503` when it expired. Nothing in the configuration
file mentioned that number, and `idleTimeout` had no influence on it.

The limit is now implemented with Jetty 12's `QoSHandler`, whose own defaults are different again
(a 1024-long queue, no waiting time limit). Rather than inherit either set of implicit defaults,
grobid-quantities now sets all three bounds explicitly from the configuration file, so a `503`
can always be traced back to a value that is written down somewhere.

If you are seeing `503`s under load, raise `maxParallelRequests` (and give the service the memory
and cores to match — see [Getting started](gettingStarted.md)); changing `idleTimeout` will not
help.
