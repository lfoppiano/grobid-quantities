# Getting started

> :warning: Grobid, and therefore grobid-quantities, [is not supported running natively on Windows](https://grobid.readthedocs.io/en/latest/Frequently-asked-questions/#windows-related-issues). Windows users should use the Docker image and call the service over the REST API. Note that Grobid also reports issues with the Windows Subsystem for Linux, so WSL is not a reliable alternative.

> :information_source: Apple Silicon (M1 and later) is supported: Grobid ships the native Wapiti library for `mac_arm-64` and `lin_arm-64`, so the CRF models run natively on ARM. Earlier versions of this page described ARM support as "under development" - that is no longer the case. Running the *deep learning* models on ARM depends on your TensorFlow install rather than on Grobid; see [Deep Learning models](https://grobid.readthedocs.io/en/latest/Deep-Learning-models/).

> :warning: Since grobid-quantities 0.7.3 (using grobid 0.7.3), we've extended the support to JDK after version 11. This requires specifying the [java.library.path]{.title-ref} explicitly. *All these issues are solved by using Docker containers*.

## Upgrade

### 0.8.2 to 0.9.1

This is the largest upgrade since 0.7.x. grobid-quantities follows Grobid, so most of the work is
Grobid's [0.9.0](https://grobid.readthedocs.io/en/latest/Upgrading/#upgrading-to-090) and
[0.9.1](https://grobid.readthedocs.io/en/latest/Upgrading/#upgrading-to-091) upgrades; the notes
below are what that means for grobid-quantities specifically. **An existing 0.8.2 installation
will not work as-is** — the build environment, the deep learning runtime and the models all
change.

#### At a glance

| | 0.8.2 | 0.9.1 |
|---|---|---|
| Grobid | 0.8.2 | 0.9.1 |
| JDK | 17 | **21** |
| Gradle | 7.2 | **9.0** (wrapper bundled) |
| Web framework | Dropwizard 4 / Jetty 11 | **Dropwizard 5 / Jetty 12** (Jakarta EE 10) |
| Python (DL only) | 3.7–3.8 | **3.10–3.11** |
| TensorFlow (DL only) | 2.9.x | **2.17** |
| DeLFT (DL only) | 0.3.4 | **>= 0.4.1** |
| JEP (DL only) | 4.0.1 | **4.3.1** |

#### JDK 21 and Gradle 9

Grobid 0.9.0 requires **OpenJDK 21**; 17 is no longer enough. The Gradle wrapper is committed, so
`./gradlew` picks up Gradle 9 by itself — just make sure it runs on a JDK 21.

#### Models must be updated

The quantities, units and values models have to be reinstalled — the ones from 0.8.2 will not
give correct results, and the deep learning ones will not load at all under DeLFT 0.4.x /
TensorFlow 2.17:

```shell
cd PATH-TO-GROBID/grobid-quantities
./gradlew installModels
```

`installModels` copies the CRF models shipped in the repository and clones the deep learning ones
from [`sciencialab/grobid-quantities-models`](https://huggingface.co/sciencialab/grobid-quantities-models)
on the HuggingFace Hub. That repository stores the large files with Xet, so
[git-xet](https://hf.co/docs/hub/git-xet) has to be installed first, otherwise the clone leaves
pointer stubs instead of model weights.

Grobid's own models under `grobid-home/models/` were retrained in 0.9.0 as well, so pull them
along with the Grobid code. If you maintain **custom-trained** models, they must be retrained
against DeLFT >= 0.4.1 / TensorFlow 2.17.

#### Deep learning environment

Only relevant if you run the DL models locally rather than through Docker. The stack moved to
Python 3.10–3.11, TensorFlow 2.17, DeLFT >= 0.4.1 and JEP 4.3.1, which needs a **fresh Python
environment** — see Grobid's [Deep Learning models](https://grobid.readthedocs.io/en/latest/Deep-Learning-models/)
page. Note that the `java.library.path` used to start the service points into that environment,
so the `python3.9` in the command lines further down this page becomes `python3.11` (or whichever
version your environment uses).

#### Configuration file

Dropwizard 5 validates the `server:` block more strictly than Dropwizard 4 and **aborts at
startup** on options it does not recognise. If you kept the shipped `config.yml` there is nothing
to do; if you maintain a customised one:

- Remove `server.maxQueuedRequests`. This is the line the [0.7.3 to 0.8.0](#073-to-080) step below
  told you to add - it is no longer accepted:

    ```diff
     server:
       type: custom
       ...
       maxThreads: 2048
    -  maxQueuedRequests: 2048
    ```

- Remove any `views:` block. It was never wired to anything here, and since the application
  enables `FAIL_ON_UNKNOWN_PROPERTIES` it now stops the service with
  `Unrecognized field at: views`:

    ```diff
    -views:
    -  .mustache:
    -    cache: false
    ```

- Keep `idleTimeout` and `acceptQueueSize` on the connector, not at the `server:` level.

You can check a configuration file without starting the service:

```shell
java -jar build/libs/grobid-quantities-{version}-onejar.jar check resources/config/config.yml
```

#### Docker

The image now builds on `lfoppiano/grobid:0.9.1-full`. If you derive your own image from it,
the JEP path in `GROBID_QUANTITIES_OPTS` moved from `python3.8` to `python3.11`.

### 0.8.0 to 0.8.2

No breaking changes

### 0.7.3 to 0.8.0

#### Grobid models 

In version 0.8.0, we have updated all ML models that need to be updated by running `./gradlew copyModels`.

#### Configuration file 

The configuration file needs to be updated to follow the Dropwizard 4 format, which has changed slightly. 

The section 
```yaml
views:
    .mustache:
        cache: false

server:
    type: custom
    applicationConnectors:
        - type: http
          port: 8060
          idleTimeout: 120 seconds
    adminConnectors:
        - type: http
          port: 8061
    registerDefaultExceptionMappers: false
    maxThreads: 2048
    requestQueueMaxSize: 2048
    acceptQueueSize: 2048
```

Should become: 

```yaml
server:
  type: custom
  applicationConnectors:
    - type: http
      port: 8060
      idleTimeout: 120 seconds
      acceptQueueSize: 2048
      
  adminConnectors:
    - type: http
      port: 8061
  registerDefaultExceptionMappers: false
  maxThreads: 2048
  requestQueueMaxSize: 2048
```

### 0.7.2 to 0.7.3

#### Grobid models

In version 0.7.3, we have updated the DeLFT models. The DL models must be updated by running `./gradlew copyModels`.

#### JDK Update

The version 0.7.3 enables the support for running with JDK > 11. 
We recommend running it with JDK 17. Running grobid-quantities with gradle (`./gradlew clean run`) is already supported in the `build.gradle`. 
Running grobid-quantities via the JAR file requires an additional parameter to set the `java.path`:

- Linux: `-Djava.library.path=../grobid-home/lib/lin-64:../grobid-home/lib/lin-64/jep`
- Mac (arm): `-Djava.library.path=.:/usr/lib/java:../grobid-home/lib/mac_arm-64:{MY_VIRTUAL_ENV}/jep/lib:{MY_VIRTUAL_ENV}/jep/lib/python3.9/site-packages/jep --add-opens java.base/java.lang=ALL-UNNAMED`
- Mac (intel): `-Djava.library.path=.:/usr/lib/java:../grobid-home/lib/mac-64:{MY_VIRTUAL_ENV}/jep/lib:{MY_VIRTUAL_ENV}/jep/lib/python3.9/site-packages/jep --add-opens java.base/java.lang=ALL-UNNAMED`

With `MY_VIRTUAL_ENV` I use `/Users/lfoppiano/anaconda3/envs/jep`

### 0.7.1 to 0.7.2

In version 0.7.2, we have updated the DeLFT models. The DL models must
be updated by running `./gradlew copyModels`.

### 0.7.0 to 0.7.1

In version 0.7.1, a new version of DeLFT using Tensorflow 2.x is used.
The DL models must be updated by running `./gradlew copyModels`.

### 0.6.0 to 0.7.0

In version 0.7.0, the models have been updated, therefore it is required
to run a `./gradlew copyModels` to have properly results, especially for
what concerns the unit normalization.

## Requirements

Grobid-quantities loads the models it needs on the first request and keeps them in memory, so
the memory footprint is roughly constant once the service is warm and does not grow with the
number of requests.

| Usage                                                | Heap (`-Xmx`) | Notes                                                            |
|------------------------------------------------------|---------------|------------------------------------------------------------------|
| Text only (`processQuantityText`, `processUnitsText`) | 2 GB          | ~1.5 GB is used once the CRF models are loaded                    |
| PDF (`annotateQuantityPDF`)                           | 4 GB          | the Grobid full-text models and the PDF parsing add to the above  |
| Deep learning models (DeLFT/BERT instead of CRF)      | 8 GB          | plus a GPU if you want a reasonable throughput                    |

These are the figures for a single request at a time. Concurrency is bounded by
`maxParallelRequests` in `config.yml`; count roughly one additional core per parallel request.

The text-only figure was measured on the CRF models (see
[#108](https://github.com/lfoppiano/grobid-quantities/issues/108)); the other two are indicative
and depend on the documents and on the models you enable.

A machine with less memory than the above will typically fail with
`java.lang.OutOfMemoryError: Java heap space` **while answering the first request**, not at
startup, because the models are loaded lazily.

Grobid-quantities also requires *JDK 21* (since version 0.9.0) and a Grobid installation - see
below.

## Install and build

#### Docker containers

The simplest way to run grobid-quantities is via docker containers.

The Grobid-quantities repository provides a configuration file for docker: `resources/config/config-docker.yml`, which should work out of the box, although we recommend to **check the configuration** (e.g., to enable modules using deep learning).

To run the container use: 
```shell 
docker run \--rm \--init -p 8060:8060 -p 8061:8061 -v resources/config/config-docker.yml:/opt/grobid/grobid-quantities/config.yml:ro lfoppiano/grobid-quantities:0.7.2
```

The container will respond on port <http://localhost:8060>, and 8061 for the admin interface.

#### Local installation

Grobid-quantities requires *JDK 21* (since version 0.9.0, which follows Grobid 0.9.0) and Grobid to be installed.

First install the latest version of GROBID as explained by the [documentation](http://grobid.readthedocs.org).

Grobid-quantities root directory needs to be placed as sibling sub-project inside Grobid directory:

```shell
cp -r grobid-quantities grobid/
```

The easier is to clone directly within the Grobid directory.

Then, build everything with: :

```shell
cd PATH-TO-GROBID/grobid-quantities/

./gradlew copyModels
./gradlew clean build
```

You should have the directories of the models `quantities*`, `units*`
and `values*` inside `../grobid-home/models`

Run some test: :

```shell
cd PATH-TO-GROBID/grobid-quantities

./gradlew test
```

##### Start and use the service

Grobid-quantities can be run with the following command: :

```shell
    java -Djava.library.path=../grobid-home/lib/{arch}/:{virtual_env_path}/lib:{virtual_env_path}/lib/python3.9/site-packages/jep -jar build/libs/grobid-quantities-{version}-onejar.jar server resources/config/config.yml
```

> :warning: The command requires the following parameters: `{arch}` is the subdirectory under `grobid-home/lib` that support the following architectures: `lin-64`, `mac-64`, `mac_arm-64`. `{virtual_env_path}` is the path to the virtualenv (e.g. in my case is something like `/Users/lfoppiano/anaconda3/envs/jep/`)


## Accessing the service

Grobid-quantities provides a graphical demo accessible at `http://localhost:8060`, and a REST API, reachable under `http://localhost:8060/service` and documented in the [REST API](restAPI.md).

To test the API, is possible to run a simple text using `curl`:

```shell
curl -X POST -F "text=I've lost two minutes." localhost:8060/service/processQuantityText
```

> :information_source: The model is designed and trained to work at *paragraph level*. The expected text input to the parser is a paragraph or a text segment of similar size, not a complete document. In case you have a long textual document, it is better either to exploit existing structures (e.g. XML/HTML `<p>` elements) to initially segment it into paragraphs or sentences, or to apply an automatic paragraph/sentence segmentation. Then send them separately to grobid-quantities to be processed.


#### Using the python client

The easiest way to interact with the server is to use the Python Client. 
It removes the complexity of dealing with the output data, and managing single or multi-thread processing. 
More information can be found at the [Python client GitHub page](https://github.com/lfoppiano/grobid-quantities-python-client).

## Timeouts and parallel requests

Two settings of `config.yml` are often mistaken for each other:

- `server.applicationConnectors[].idleTimeout` is a **connection** timeout: it bounds how long a
  connection may stay idle in terms of I/O. Since the migration to Jetty 12 it does *not* bound
  how long the service may take to compute a response - a request that takes minutes completes
  normally even with a short `idleTimeout`.
- `maxParallelRequests` bounds how many requests are processed at the same time. Requests over
  that limit are queued, and rejected with **503** when they cannot be served. This, and not the
  idle timeout, is what a load test hits first. The queue itself is bounded by
  `requestQueueMaxSize` and `requestQueueMaxWait`, documented in the
  [REST API page](restAPI.md#maximum-parallel-requests-limit).

Both halves of issue [#159](https://github.com/lfoppiano/grobid-quantities/issues/159) came from
that second setting, not from `idleTimeout`. The reporter raised `idleTimeout` to 120 seconds and
still saw requests fail with **503** after almost exactly 30 seconds: the limit was then enforced
by Jetty 9's `QoSFilter`, which suspended over-limit requests using the servlet *default* async
timeout of 30 seconds before rejecting them. That number appeared nowhere in the configuration,
and `idleTimeout` had no bearing on it.

Verified on the current version: a single request taking ~70 seconds returns 200 with the
complete response, with `idleTimeout` set to 120 seconds and also with it set to 5 seconds. The
queue bounds are now set explicitly rather than inherited from the Jetty defaults, so a 503 can
be traced back to a value written in `config.yml`.
