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

## Latest version

The latest released version of grobid-quantities
is [0.7.1](https://github.com/kermitt2/grobid-quantities/releases/tag/0.7.1). The current development version is
0.7.2-SNAPSHOT.

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

| Labels          | CRF            |             |               | **BidLSTM_CRF** |             |               | **BidLSTM_CRF_FEATURES** |             |               | **SciBERT**       |               |                |
|-----------------|----------------|-------------|---------------|-----------------|-------------|---------------|--------------------------|-------------|---------------|-------------------|---------------|----------------|
| Metrics         | **Precision**  | **Recall**  | **F1-Score**  | **Precision**   | **Recall**  | **F1-Score**  | **Precision**            | **Recall**  | **F1-Score**  | **Precision**     | **Recall**    | **F1-Score**   | 
| `<unitLeft>`    | 88.74          | 83.19       | 85.87         | 88.56           | 92.07       | 90.28         | 88.91                    | 92.20       | 90.53         | 93.26             | 70.39         | 80.23          | 
| `<unitRight>`   | 30.77          | 30.77       | 30.77         | 24.75           | 30.77       | 27.42         | 21.73                    | 30.77       | 25.41         | 17.31             | 21.54         | 19.18          | 
| `<valueAtomic>  | 76.29          | 78.66       | 77.46         | 78.14           | 86.06       | 81.90         | 78.21                    | 86.20       | 82.01         | 83.41             | 69.33         | 75.72          | 
| `<valueBase>`   | 84.62          | 62.86       | 72.13         | 83.51           | 94.86       | 88.61         | 83.36                    | 97.14       | 89.72         | 90.24             | 59.43         | 71.53          |  
| `<valueLeast>`  | 77.68          | 69.05       | 73.11         | 82.14           | 60.63       | 69.67         | 80.73                    | 60.63       | 69.12         | 79.31             | 53.49         | 63.88          | 
| `<valueList>`   | 45.45          | 18.87       | 26.67         | 62.15           | 10.19       | 17.34         | 73.33                    | 8.68        | 15.33         | 56.77             | 30.95         | 40.01          | 
| `<valueMost>`   | 71.62          | 54.64       | 61.99         | 77.64           | 68.25       | 72.61         | 77.25                    | 70.31       | 73.58         | 73.72             | 51.34         | 60.52          | 
| `<valueRange>`  | 100            | 97.14       | 98.55         | 96.72           | 100.00      | 98.32         | 94.05                    | 98.86       | 96.38         | 99.09             | 60.00         | 74.74          | 
| --              |                |             |               |                 |             |               |                          |             |               |                   |               |                | 
| All (micro avg) | 80.08          | 75          | 77.45         | 81.81           | 81.73       | 81.76         | 81.76                    | 81.94       | 81.85         | 84.41             | 64.70         | 73.25          | 

#### Units

|                 | **CRF**             |            |              | **BidLSTM_CRF**  |            |                | **BidLSTM_CRF_FEATURES** |            |              | **SciBERT**   |            |              |
|-----------------|---------------------|------------|--------------|------------------|------------|----------------|--------------------------|------------|--------------|---------------|------------|--------------|
| Labels          | **Precision**       | **Recall** | **F1-Score** | **Precision**    | **Recall** | **F1-Score**   | **Precision**            | **Recall** | **F1-Score** | **Precision** | **Recall** | **F1-Score** |
| `<base>`        | 80.57               | 82.34      | 81.45        | 56.01            | 50.34      | 53.02          | 63.89                    | 61.11      | 62.45        | 73.23         | 72.51      | 72.85        |
| `<pow>`         | 72.65               | 74.45      | 73.54        | 93.70            | 62.38      | 74.88          | 90.83                    | 70.24      | 79.19        | 70.72         | 49.41      | 58.11        |
| `<prefix>`      | 93.8                | 84.69      | 89.02        | 80.31            | 85.25      | 82.54          | 82.55                    | 83.51      | 82.95        | 74.60         | 82.75      | 78.45        |   
| --              |                     |            |              |                  |            |                |                          |            |              |               |            |              |
| All (micro avg) | 80.73               | 80.6       | 80.66        | 70.19            | 60.88      | 65.20          | 74.48                    | 68.27      | 71.30        | 72.91         | 68.09      | 70.41        |
 

#### Values

|                 | **CRF**          |            |              | **BidLSTM_CRF** |            |          | **BidLSTM_CRF_FEATURES** |            |                | **SciBERT**   |            |              |
|-----------------|------------------|------------|--------------|-----------------|------------|----------|--------------------------|------------|----------------|---------------|------------|--------------|
| Labels          | **Precision**    | **Recall** | **F1-Score** | **Precision**   | **Recall** | F1-Score | **Precision**            | **Recall** | **F1-Score**   | **Precision** | **Recall** | **F1-Score** |
| `<alpha>`       | 97.66            | 99.21      | 98.43        | 99.53           | 100.00     | 99.76    | 99.37                    | 99.37      | 99.68          | 99.84         | 100.00     | 99.92        |
| `<base>`        | 76.92            | 76.92      | 76.92        | 98.18           | 76.92      | 86.23    | 96.36                    | 96.36      | 85.51          | 85.11         | 84.62      | 84.70        |
| `<number>`      | 97.8             | 98.52      | 98.16        | 98.93           | 97.73      | 98.32    | 98.83                    | 98.83      | 98.50          | 98.46         | 98.94      | 98.70        |
| `<pow>`         | 83.33            | 76.92      | 80           | 100.00          | 76.92      | 86.96    | 96.36                    | 96.36      | 85.51          | 92.14         | 90.77      | 91.31        |
| `<time>`        | 70.18            | 86.96      | 77.67        | 73.17           | 96.52      | 83.05    | 78.43                    | 78.43      | 85.01          | 89.44         | 87.83      | 88.52        |
| --              |                  |            |              |                 |            |          |                          |            |                |               |            |              |
| All (micro avg) | 95.81            | 97.52      | 96.66        | 97.41           | 97.42      | 97.41    | 97.62                    | 97.62      | 97.64          | 97.95         | 98.28      | 98.11        |


<details>
  <summary>Previous evaluations</summary>

Previous evaluation were provided using 10-fold cross-validation (with average metrics over the 10 folds).

The `CRF` model was evaluated on the 30/04/2020.
The `BidLSTM_CRF_FEATURES` model was evaluated on the 28/11/2021

#### Quantities

|                 | CRF             |              |                 | BidLSTM_CRF_FEATURES       |              |          |
|-----------------|-----------------|--------------|-----------------|----------------------------|--------------|----------|
| Labels          | **Precision**   | **Recall**   | **F1-Score**    | **Precision**              | **Recall**   | F1-Score |
| `<unitLeft>`    | 96.45           | 95.06        | 95.74           | 95.17                      | 96.67        | 95.91    |    
| `<unitRight>`   | 88.96           | 68.65        | 75.43           | 92.52                      | 83.64        | 87.69    |    
| `<valueAtomic>  | 85.75           | 85.35        | 85.49           | 81.74                      | 89.21        | 85.30    |    
| `<valueBase>`   | 73.06           | 66.43        | 68.92           | 100.00                     | 75.00        | 85.71    |     
| `<valueLeast>`  | 85.68           | 79.03        | 82.07           | 89.24                      | 82.25        | 85.55    |    
| `<valueList>`   | 68.38           | 53.31        | 58.94           | 75.27                      | 75.33        | 75.12    |  
| `<valueMost>`   | 83.67           | 75.82        | 79.42           | 89.02                      | 81.56        | 85.10    |  
| `<valueRange>`  | 90.25           | 88.58        | 88.86           | 100.00                     | 96.25        | 97.90    |  
| --              |                 |              |                 |                            |              |          |  
| All (micro avg) | 88.96           | 85.4         | 87.14           | 87.23                      | 89.00        | 88.10    |    
 

#### Units

CRF was updated the 10/02/2021

|                 | CRF           |            |              | BidLSTM_CRF_FEATURES |            |           |
|-----------------|---------------|------------|--------------|----------------------|------------|-----------|
| Labels          | **Precision** | **Recall** | **F1-Score** | **Precision**        | **Recall** | F1-Score  |
| `<base>`        | 98.82         | 99.14      | 98.98        | 98.26                | 98.52      | 98.39     |    
| `<pow>`         | 97.62         | 98.56      | 98.08        | 100.00               | 98.57      | 99.28     |    
| `<prefix>`      | 99.5          | 98.76      | 99.13        | 98.89                | 97.75      | 98.30     |    
| --              |               |            |              |                      |            |           |  
| All (micro avg) | 98.85         | 99.01      | 98.93        | 98.51                | 98.39      | 98.45     |


#### Values

|                 | CRF           |            |              | BidLSTM_CRF_FEATURES |            |           |
|-----------------|---------------|------------|--------------|----------------------|------------|-----------|
| Labels          | **Precision** | **Recall** | **F1-Score** | **Precision**        | **Recall** | F1-Score  |
| `<alpha>`       | 96.9          | 98.84      | 97.85        | 99.41                | 99.55      | 99.48     |    
| `<base>`        | 85.14         | 74.48      | 79           | 96.67                | 100.00     | 98.00     |    
| `<number>`      | 98.07         | 99.05      | 98.55        | 99.55                | 98.68      | 99.11     |    
| `<pow>`         | 80.05         | 76.33      | 77.54        | 72.50                | 75.00      | 73.50     |     
| `<time>`        | 73.07         | 86.82      | 79.26        | 80.84                | 100.00     | 89.28     |
| --              |               |            |              |                      |            |           |  
| All (micro avg) | 96.15         | 97.95      | 97.4         | 98.49                | 98.66      | 98.57     |

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
