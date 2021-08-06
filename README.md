# grobid-quantities

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Documentation Status](https://readthedocs.org/projects/grobid-quantities/badge/?version=latest)](https://readthedocs.org/projects/grobid-quantities/?badge=latest)
[![CircleCI](https://circleci.com/gh/circleci/circleci-docs.svg?style=svg)](https://circleci.com/gh/kermitt2/grobid-quantities)
[![Coverage Status](https://coveralls.io/repos/kermitt2/grobid-quantities/badge.svg)](https://coveralls.io/r/kermitt2/grobid-quantities)

__Work in progress.__

The goal of this GROBID module is to recognize in textual documents any expressions of measurements (e.g. _pressure_, _temperature_, etc.), to parse and normalization them, and finally to convert these measurements into SI units. 
We focus our work on technical and scientific articles (text, XML and PDF input) and patents (text and XML input). 

![GROBID Quantity Demo](doc/img/Screenshot2.png)

As part of this task we support the recognition of the different value representation: numerical, alphabetical, exponential and date/time expressions. 

![Grobid Quantity Demo](doc/img/Screenshot7.png)

Finally we support the identification of the "quantified" substance related to the measure, e.g. _silicon nitride powder_ in 

![GROBID Quantity Demo](doc/img/Screenshot5.png)

As the other GROBID models, the module relies only on machine learning and it uses linear CRF. 
The normalisation of quantities is handled by the java library [Units of measurement](http://unitsofmeasurement.github.io/).  

## Latest version

The latest released version of grobid-quantities is 0.6.0. The current development version is 0.6.1. 

## Documentation

You can find the latest documentation [here](http://grobid-quantities.readthedocs.io). 

## Evaluation
The results (Precision, Recall, F-score) for all the models have been obtained using 10-fold cross-validation (average metrics over the 10 folds). 

Evaluated on the 30/04/2020.

### Quantities     

| Labels          | Precision  | Recall      |  F1-Score     |
|-----------------|------------|-------------|---------------|
| `<unitLeft>`    | 96.45      |   95.06     |   95.74       |    
| `<unitRight>`   | 88.96      |   68.65     |   75.43       |    
| `<valueAtomic>` | 85.75      |   85.35     |   85.49       |    
| `<valueBase>`   | 73.06      |   66.43     |   68.92       |     
| `<valueLeast>`  | 85.68      |   79.03     |   82.07       |    
| `<valueList>`   | 68.38      |   53.31     |   58.94       |  
| `<valueRange>`  | 90.25      |   88.58     |   88.86       |  
| all (micro avg.)| 88.96      |   85.4      |   87.14       |      

### Units

| Labels          | Precision  | Recall      |  F1-Score     |
|---------------- |------------|-------------|---------------|
| `<base>`        | 98.95      |  99.02      |   98.98       |    
| `<pow>`         | 97.2       |  98.49      |   97.83       |    
| `<prefix>`      | 98.34      |  98.47      |   98.38       |    
| all (micro avg.)| 98.7       |  98.89      |   98.8        |

### Values 

| Labels          | Precision  | Recall      |  F1-Score     |
|-----------------|------------|-------------|---------------|
| `<alpha>`       | 96.9       |   98.84     |   97.85       |    
| `<base>`        | 85.14      |   74.48     |   79          |    
| `<number>`      | 98.07      |   99.05     |   98.55       |    
| `<pow>`         | 80.05      |   76.33     |   77.54       |     
| `<time>`        | 73.07      |   86.82     |   79.26       |    
| all (micro avg.)| 96.15      |   97.95     |   97.4        |

The current average results have been calculated using micro average which provides more realistic results by giving different weights to labels based on their frequency.
The [paper](https://doi.org/10.1145/3342558.3345411) "Automatic Identification and Normalisation of Physical Measurements in Scientific Literature", published in September 2019 reported average evaluation based on macro average. 

## Acknowledgement 

This project has been created and developed by [science-miner](https://science-miner.com) since 2015, with additional support by [Inria](http://www.inria.fr), in Paris (France) and the [National Institute for Materials Science](http://www.nims.go.jp), in [Tsukuba](https://en.wikipedia.org/wiki/Tsukuba,_Ibaraki) (Japan).

## How to cite

If you want to cite this work, please simply refer to the github project with optionally the [Software Heritage](https://www.softwareheritage.org/) project-level permanent identifier:

```
grobid-quantities (2015-2021) <https://github.com/kermitt2/grobid-quantities>, swh:1:dir:dbf9ee55889563779a09b16f9c451165ba62b6d7
```

Here's a BibTeX entry using the [Software Heritage](https://www.softwareheritage.org/) project-level permanent identifier:

```bibtex
@misc{grobid-quantities,
    title = {grobid-quantities},
    howpublished = {\url{https://github.com/kermitt2/grobid-quantities}},
    publisher = {GitHub},
    year = {2015--2021},
    archivePrefix = {swh},
    eprint = {1:dir:dbf9ee55889563779a09b16f9c451165ba62b6d7}
}
```

## License

GROBID and grobid-quantities are distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 

The documentation is distributed under [CC-0](https://creativecommons.org/publicdomain/zero/1.0/) license and the annotated data under [CC-BY](https://creativecommons.org/licenses/by/4.0/) license.

If you contribute to grobid-quantities, you agree to share your contribution following these licenses. 

Contact: Patrice Lopez (<patrice.lopez@science-miner.com>), Luca Foppiano (<luca@foppiano.org>)
