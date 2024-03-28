# Getting started

> :warning: Grobid and grobid-quantities are [not compatible with Windows](https://grobid.readthedocs.io/en/latest/Troubleshooting/#windows-related-issues) and limited on Apple M1. While Windows users can easily use Grobid and grobid-quantities through docker containers, the support for grobid on ARM is under development, see the [latest discussion](https://github.com/kermitt2/grobid/issues/1014).

> :warning: Since grobid-quantities 0.7.3 (using grobid 0.7.3), we extended the support to JDK after version 11. This requires specifying the [java.library.path]{.title-ref} explicitly. Obviously, *all these issues are solved by using Docker containers*.

## Upgrade

### 0.7.3 to 0.8.0

#### Grobid models 

In version 0.8.0, we have updated all ML models which needs to be updated by running `./gradlew copyModels`.

#### Configuration file 

The configuration file needs to be updated to follow the Dropwizard 4 format, which has changed slighly. 

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
    maxQueuedRequests: 2048
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
  maxQueuedRequests: 2048
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

Grobid-quantities requires *JDK 1.8 or greater*, and Grobid to be installed. Since version 0.7.3 we recommend to use *JDK 17 or greater*.

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
