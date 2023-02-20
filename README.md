# grobid-quantities

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Documentation Status](https://readthedocs.org/projects/grobid-quantities/badge/?version=latest)](https://readthedocs.org/projects/grobid-quantities/?badge=latest)
[![CircleCI](https://circleci.com/gh/kermitt2/grobid-quantities.svg?style=svg)](https://circleci.com/gh/kermitt2/grobid-quantities)
[![Coverage Status](https://coveralls.io/repos/kermitt2/grobid-quantities/badge.svg)](https://coveralls.io/r/kermitt2/grobid-quantities)
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
is [0.7.2](https://github.com/kermitt2/grobid-quantities/releases/tag/0.7.2). The current development version is
0.7.3-SNAPSHOT.

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

## Evaluation

The results (Precision, Recall, F-score) for all the models have been obtained using an holdout set.
For DL models we provide the average over 5 runs.
Update on the 27/10/2022

#### Quantities

| Labels          | CRF           |            |              | **BidLSTM_CRF** |            |              |
|-----------------|---------------|------------|--------------|-----------------|------------|--------------|
| Metrics         | **Precision** | **Recall** | **F1-Score** | **Precision**   | **Recall** | **F1-Score** |
| `<unitLeft>`    | 88.74         | 83.19      | 85.87        | 88.56           | 92.07      | 90.28        |
| `<unitRight>`   | 30.77         | 30.77      | 30.77        | 24.75           | 30.77      | 27.42        |
| `<valueAtomic>  | 76.29         | 78.66      | 77.46        | 78.14           | 86.06      | 81.90        |
| `<valueBase>`   | 84.62         | 62.86      | 72.13        | 83.51           | 94.86      | 88.61        |
| `<valueLeast>`  | 77.68         | 69.05      | 73.11        | 82.14           | 60.63      | 69.67        |
| `<valueList>`   | 45.45         | 18.87      | 26.67        | 62.15           | 10.19      | 17.34        |
| `<valueMost>`   | 71.62         | 54.64      | 61.99        | 77.64           | 68.25      | 72.61        |
| `<valueRange>`  | 100           | 97.14      | 98.55        | 96.72           | 100.00     | 98.32        |
| --              |               |            |              |                 |            |              |
| All (micro avg) | 80.08         | 75         | 77.45        | 81.81           | 81.73      | 81.76        |

#### Units

|                 | **CRF**       |            |              | **BidLSTM_CRF** |            |              |
|-----------------|---------------|------------|--------------|-----------------|------------|--------------|
| Labels          | **Precision** | **Recall** | **F1-Score** | **Precision**   | **Recall** | **F1-Score** |
| `<base>`        | 80.57         | 82.34      | 81.45        | 56.01           | 50.34      | 53.02        |
| `<pow>`         | 72.65         | 74.45      | 73.54        | 93.70           | 62.38      | 74.88        |
| `<prefix>`      | 93.8          | 84.69      | 89.02        | 80.31           | 85.25      | 82.54        |
| --              |               |            |              |                 |            |              |
| All (micro avg) | 80.73         | 80.6       | 80.66        | 70.19           | 60.88      | 65.20        |

#### Values

|                 | **CRF**       |            |              | **BidLSTM_CRF** |            |          |
|-----------------|---------------|------------|--------------|-----------------|------------|----------|
| Labels          | **Precision** | **Recall** | **F1-Score** | **Precision**   | **Recall** | F1-Score |
| `<alpha>`       | 97.66         | 99.21      | 98.43        | 99.53           | 100.00     | 99.76    |
| `<base>`        | 76.92         | 76.92      | 76.92        | 98.18           | 76.92      | 86.23    |
| `<number>`      | 97.8          | 98.52      | 98.16        | 98.93           | 97.73      | 98.32    |
| `<pow>`         | 83.33         | 76.92      | 80           | 100.00          | 76.92      | 86.96    |
| `<time>`        | 70.18         | 86.96      | 77.67        | 73.17           | 96.52      | 83.05    |
| --              |               |            |              |                 |            |          |
| All (micro avg) | 95.81         | 97.52      | 96.66        | 97.41           | 97.42      | 97.41    |

<details>
  <summary>Previous evaluations</summary>

Previous evaluation were provided using 10-fold cross-validation (with average metrics over the 10 folds).

The `CRF` model was evaluated on the 30/04/2020.
The `BidLSTM_CRF_FEATURES` model was evaluated on the 28/11/2021

#### Quantities

|                 | CRF           |            |              | BidLSTM_CRF_FEATURES |            |          |
|-----------------|---------------|------------|--------------|----------------------|------------|----------|
| Labels          | **Precision** | **Recall** | **F1-Score** | **Precision**        | **Recall** | F1-Score |
| `<unitLeft>`    | 96.45         | 95.06      | 95.74        | 95.17                | 96.67      | 95.91    |    
| `<unitRight>`   | 88.96         | 68.65      | 75.43        | 92.52                | 83.64      | 87.69    |    
| `<valueAtomic>  | 85.75         | 85.35      | 85.49        | 81.74                | 89.21      | 85.30    |    
| `<valueBase>`   | 73.06         | 66.43      | 68.92        | 100.00               | 75.00      | 85.71    |     
| `<valueLeast>`  | 85.68         | 79.03      | 82.07        | 89.24                | 82.25      | 85.55    |    
| `<valueList>`   | 68.38         | 53.31      | 58.94        | 75.27                | 75.33      | 75.12    |  
| `<valueMost>`   | 83.67         | 75.82      | 79.42        | 89.02                | 81.56      | 85.10    |  
| `<valueRange>`  | 90.25         | 88.58      | 88.86        | 100.00               | 96.25      | 97.90    |  
| --              |               |            |              |                      |            |          |  
| All (micro avg) | 88.96         | 85.4       | 87.14        | 87.23                | 89.00      | 88.10    |    

#### Units

CRF was updated the 10/02/2021

|                 | CRF           |            |              | BidLSTM_CRF_FEATURES |            |          |
|-----------------|---------------|------------|--------------|----------------------|------------|----------|
| Labels          | **Precision** | **Recall** | **F1-Score** | **Precision**        | **Recall** | F1-Score |
| `<base>`        | 98.82         | 99.14      | 98.98        | 98.26                | 98.52      | 98.39    |    
| `<pow>`         | 97.62         | 98.56      | 98.08        | 100.00               | 98.57      | 99.28    |    
| `<prefix>`      | 99.5          | 98.76      | 99.13        | 98.89                | 97.75      | 98.30    |    
| --              |               |            |              |                      |            |          |  
| All (micro avg) | 98.85         | 99.01      | 98.93        | 98.51                | 98.39      | 98.45    |

#### Values

|                 | CRF           |            |              | BidLSTM_CRF_FEATURES |            |          |
|-----------------|---------------|------------|--------------|----------------------|------------|----------|
| Labels          | **Precision** | **Recall** | **F1-Score** | **Precision**        | **Recall** | F1-Score |
| `<alpha>`       | 96.9          | 98.84      | 97.85        | 99.41                | 99.55      | 99.48    |    
| `<base>`        | 85.14         | 74.48      | 79           | 96.67                | 100.00     | 98.00    |    
| `<number>`      | 98.07         | 99.05      | 98.55        | 99.55                | 98.68      | 99.11    |    
| `<pow>`         | 80.05         | 76.33      | 77.54        | 72.50                | 75.00      | 73.50    |     
| `<time>`        | 73.07         | 86.82      | 79.26        | 80.84                | 100.00     | 89.28    |
| --              |               |            |              |                      |            |          |  
| All (micro avg) | 96.15         | 97.95      | 97.4         | 98.49                | 98.66      | 98.57    |

</details>

The current average results have been calculated using micro average which provides more realistic results by giving
different weights to labels based on their frequency.
The [paper](https://hal.inria.fr/hal-02294424) "Automatic Identification and Normalisation of Physical Measurements in
Scientific Literature", published in September 2019 reported average evaluation based on macro average.

## Acknowledgement

This project has been created and developed by [science-miner](https://science-miner.com) since 2015, with additional
support by [Inria](http://www.inria.fr), in Paris (France) and
the [National Institute for Materials Science](http://www.nims.go.jp),
in [Tsukuba](https://en.wikipedia.org/wiki/Tsukuba,_Ibaraki) (Japan).

## How to cite

If you want to cite this work, please simply refer to the Github project with optionally
the [Software Heritage](https://www.softwareheritage.org/) project-level permanent identifier:

```
grobid-quantities (2015-2022) <https://github.com/kermitt2/grobid-quantities>, swh:1:dir:dbf9ee55889563779a09b16f9c451165ba62b6d7
```

Here's a BibTeX entry using the [Software Heritage](https://www.softwareheritage.org/) project-level permanent
identifier:

```bibtex
@misc{grobid-quantities,
    title = {grobid-quantities},
    howpublished = {\url{https://github.com/kermitt2/grobid-quantities}},
    publisher = {GitHub},
    year = {2015--2022},
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
