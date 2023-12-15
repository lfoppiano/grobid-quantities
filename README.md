# grobid-quantities

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Documentation Status](https://readthedocs.org/projects/grobid-quantities/badge/?version=latest)](https://readthedocs.org/projects/grobid-quantities/?badge=latest)
[![Github actions](https://github.com/kermitt2/grobid-quantities/actions/workflows/ci-build.yml/badge.svg)](https://github.com/kermitt2/grobid-quantities/actions/workflows/ci-build.yml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/kermitt2/grobid-quantities/badge.svg)](https://coveralls.io/r/kermitt2/grobid-quantities)
[![Demo grobid-quantities](https://img.shields.io/website-up-down-green-red/https/lfoppiano-grobid-quantities.hf.space.svg)](https://lfoppiano-grobid-quantities.hf.space)
[![](https://jitpack.io/v/kermitt2/grobid-quantities.svg)](https://jitpack.io/#kermitt2/grobid-quantities)
[![Docker Hub](https://img.shields.io/docker/pulls/lfoppiano/grobid-quantities.svg)](https://hub.docker.com/r/lfoppiano/grobid-quantities/ "Docker Pulls")

__Work in progress.__

The goal of this GROBID module is to recognize in textual documents any expressions of measurements (e.g. _pressure_, _
temperature_, etc.), to parse and normalization them, and finally to convert these measurements into SI units.
We focus our work on technical and scientific articles (text, XML and PDF input) and patents (text and XML input).

![GROBID Quantity Demo](doc/img/Screenshot2.png)

As part of this task we support the recognition of the different value representation: numerical, alphabetical,
exponential and date/time expressions.

![Grobid Quantity Demo](doc/img/Screenshot7.png)

Finally, we support the identification of the "quantified" substance related to the measure, e.g. _silicon nitride
powder_ in

![GROBID Quantity Demo](doc/img/Screenshot5.png)

As the other GROBID models, the module relies only on machine learning and it uses linear CRF.
The normalisation of quantities is handled by the java
library [Units of measurement](http://unitsofmeasurement.github.io/).

## Online demo

Grobid-quantities can be tested with the online demo kindly offered by Huggingface
Spaces: https://lfoppiano-grobid-quantities.hf.space/

## Latest version

The latest released version of grobid-quantities
is [0.7.3](https://github.com/kermitt2/grobid-quantities/releases/tag/v0.7.3). The current development version is
0.7.4-SNAPSHOT.

### Update from 0.7.2 to 0.7.3

#### Grobid models
In version 0.7.3 we have updated the DeLFT models. The DL models must be updated by running `./gradlew copyModels`.

#### JDK Update
The version 0.7.3 enable the support for running with JDK > 11. We recommend to run it with JDK 17.
Running grobid-quantities with gradle (`./gradlew clean run`) is already supported in the `build.gradle`.
Running grobid-quantities via the JAR file requires an additional parameter to set the java.path: 
- Linux: `-Djava.library.path=../grobid-home/lib/lin-64:../grobid-home/lib/lin-64/jep`
- Mac (arm): `-Djava.library.path=.:/usr/lib/java:../grobid-home/lib/mac_arm-64:{MY_VIRTUAL_ENV}/jep/lib:{MY_VIRTUAL_ENV}/jep/lib/python3.9/site-packages/jep --add-opens java.base/java.lang=ALL-UNNAMED`
- Mac (intel): `-Djava.library.path=.:/usr/lib/java:../grobid-home/lib/mac-64:{MY_VIRTUAL_ENV}/jep/lib:{MY_VIRTUAL_ENV}/jep/lib/python3.9/site-packages/jep --add-opens java.base/java.lang=ALL-UNNAMED`
    With `MY_VIRTUAL_ENV` I use `/Users/lfoppiano/anaconda3/envs/jep`


### Update from 0.7.1 to 0.7.2

In version 0.7.2 we have updated the DeLFT models.   
The DL models must be updated by running `./gradlew copyModels`.

### Update from 0.7.0 to 0.7.1

In version 0.7.1 a new version of DeLFT using Tensorflow 2.x is used.  
The DL models must be updated by running `./gradlew copyModels`.

### Update from 0.6.0 to 0.7.0

In version 0.7.0 the models have been updated, therefore is required to run a `./gradlew copyModels` to have properly
results especially for what concern the unit normalisation.

## Documentation

You can find the latest documentation [here](http://grobid-quantities.readthedocs.io).

## Acknowledgement

This project has been created and developed by [science-miner](https://www.science-miner.com) since 2015, with additional
support by [Inria](http://www.inria.fr), in Paris (France) and
the [National Institute for Materials Science](http://www.nims.go.jp),
in [Tsukuba](https://en.wikipedia.org/wiki/Tsukuba,_Ibaraki) (Japan).

## How to cite

If you want to cite this work, please simply refer to the Github project with optionally
the [Software Heritage](https://www.softwareheritage.org/) project-level permanent identifier:

```
grobid-quantities (2015-2023) <https://github.com/kermitt2/grobid-quantities>, swh:1:dir:dbf9ee55889563779a09b16f9c451165ba62b6d7
```

Here's a BibTeX entry using the [Software Heritage](https://www.softwareheritage.org/) project-level permanent
identifier:

```bibtex
@misc{grobid-quantities,
    title = {grobid-quantities},
    howpublished = {\url{https://github.com/kermitt2/grobid-quantities}},
    publisher = {GitHub},
    year = {2015--2023},
    archivePrefix = {swh},
    eprint = {1:dir:dbf9ee55889563779a09b16f9c451165ba62b6d7}
}
```

## License

GROBID and grobid-quantities are distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).

The documentation is distributed under [CC-0](https://creativecommons.org/publicdomain/zero/1.0/) license and the
annotated data under [CC-BY](https://creativecommons.org/licenses/by/4.0/) license.

If you contribute to grobid-quantities, you agree to share your contribution following these licenses.

Contact: Patrice Lopez (<patrice.lopez@science-miner.com>), Luca Foppiano (<luca@foppiano.org>)
