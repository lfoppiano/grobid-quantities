# Evaluation scores

## End 2 end evaluation

The end-to-end evaluation was performed with the [MeasEval dataset](https://github.com/harperco/MeasEval) (SemEval-2021
Task 8).
The scores in the following table are the micro average. MeasEval was annotated to allow approximated entities, which
are not supported in grobid-quantities.

| Type (Ref)                | Matching  method | Precision | Recall | F1-score | Support |
|---------------------------|------------------|-----------|--------|----------|---------|
| Quantities (QUANT)        | strict           | 54.09     | 54.47  | 54.28    | 1137    |
| Quantities (QUANT)        | soft             | 67.02     | 67.49  | 67.26    | 1137    |
| Quantified substance (ME) | strict           | 13.82     | 9.67   | 11.38    | 615     |
| Quantified substance (ME) | soft             | 21.63     | 15.13  | 17.80    | 615     |

Note: the ME (Measured Entity) is still experimental in Grobid-quantities.

To reproduce the end-to-end evaluation, you can run the `scripts/measeval_e2e_eval.py` script (use the requirements.txt
to install the correct dependencies).

## Machine Learning Named Entities Recognition Evaluation

The scores (P: Precision, R: Recall, F1: F1-score) for all the models are performed either as 10-fold cross-validation
or using a holdout dataset.
The holdout dataset of Grobid-quantities is composed by the following examples:

- Quantities ML: 10 articles
- Units ML: [UNISCOR dataset](references.md) with around 1600 examples
- Values ML: 950 examples

For Deep learning models (BidLSTM_CRF/BidLSTM_CRF_FEATURES, BERT_CRF) models, we provide the average over 5 runs.

The models are organised as follows:

- BidLSTM_CRF is a RNN model based on (Lample et al., 2016) work, with a CRF model as activation function
- BidLSTM_CRF_FEATURES is an extension of BidLSTM_CRF that allow using layout features
- BERT_CRF is a BERT-based model obtained by fine-tuning a SciBERT encoder. Like others, the activation function is
  composed by a CRF layer.

### Results from

The evaluation was performed on the holdout dataset from the grobid-quantities dataset.
Average values are computed as Micro average.
To reproduce it, see `evaluation_doc`{.interpreted-text role="ref"}.

#### Quantities

| Labels          | CRF   |       |       |              | BERT_CRF |       |       |        | Support |
|-----------------|-------|-------|-------|--------------|----------|-------|-------|--------|---------|
| Metrics         | P     | R     | F1    |              | P        | R     | F1    | St.dev |         |
| `<unitLeft>`    | 90.26 | 83.84 | 86.93 |              | 93.13    | 89.96 | 91.52 | 0.0086 | 464     |
| `<unitRight>`   | 36.36 | 30.77 | 33.33 |              | 23.67    | 40.00 | 29.70 | 0.0139 | 13      |
| `<valueAtomic>` | 75.75 | 77.97 | 76.84 |              | 85.46    | 87.99 | 86.70 | 0.0041 | 581     |
| `<valueBase>`   | 80.77 | 60.00 | 68.85 |              | 98.75    | 90.29 | 94.33 | 0.0163 | 35      |
| `<valueLeast>`  | 76.24 | 61.11 | 67.84 |              | 84.58    | 72.22 | 77.91 | 0.0212 | 126     |
| `<valueList>`   | 27.27 | 11.32 | 16.00 |              | 61.10    | 39.62 | 47.79 | 0.0262 | 53      |
| `<valueMost>`   | 68.35 | 55.67 | 61.36 |              | 78.93    | 71.75 | 75.16 | 0.0179 | 97      |
| `<valueRange>`  | 91.18 | 88.57 | 89.86 |              | 100.00   | 91.43 | 95.52 | 0.0000 | 35      |
| ----            |
| All (micro avg) | 79.49 | 73.72 | 76.5  |              | 86.50    | 83.97 | 85.22 | 0.0031 | 1404    |

| Labels          | BidLSTM_CRF |       |       |        |        | BidLSTM_CRF_FEATURES |       |       |        | Support |
|-----------------|-------------|-------|-------|--------|--------|----------------------|-------|-------|--------|---------|
| Metrics         | P           | R     | F1    | St.dev |        | P                    | R     | F1    | St.dev |         |
| `<unitLeft>`    | 87.58       | 89.96 | 88.75 | 0.0074 |        | 86.95                | 89.57 | 88.24 | 0.0097 | 464     |
| `<unitRight>`   | 25.01       | 30.77 | 27.50 | 0.0193 |        | 23.99                | 30.77 | 26.91 | 0.0146 | 13      |
| `<valueAtomic>` | 79.52       | 85.71 | 82.49 | 0.0044 |        | 78.33                | 86.57 | 82.24 | 0.0062 | 581     |
| `<valueBase>`   | 83.84       | 97.14 | 89.97 | 0.0185 |        | 80.99                | 97.14 | 88.32 | 0.0115 | 35      |
| `<valueLeast>`  | 83.79       | 62.38 | 71.45 | 0.0294 |        | 84.37                | 60.00 | 70.06 | 0.0335 | 126     |
| `<valueList>`   | 80.12       | 13.58 | 23.05 | 0.0326 |        | 69.29                | 14.34 | 23.37 | 0.0715 | 53      |
| `<valueMost>`   | 75.91       | 70.92 | 73.22 | 0.0311 |        | 75.54                | 67.01 | 70.99 | 0.0370 | 97      |
| `<valueRange>`  | 92.87       | 94.86 | 93.84 | 0.0783 |        | 95.58                | 97.14 | 96.35 | 0.0673 | 35      |
| ----            |             |       |
| All (micro avg) | 82.12       | 81.28 | 81.70 | 0.0048 |        | 81.26                | 81.11 | 81.19 | 0.0090 | 1404    | 

#### Units

Units were evaluated using UNISCOR dataset. For more information check the section [UNISCOR](references.md#uniscor).

| Labels          | CRF   |       |       | | BERT_CRF |       |       |        | Support |
|-----------------|-------|-------|-------|-|----------|-------|-------|--------|---------|
| Metrics         | P     | R     | F1    | | P        | R     | F1    | St.dev |         |
| `<base>`        | 80.64 | 82.71 | 81.66 | | 75.42    | 75.84 | 75.52 | 0.0318 | 3228    |
| `<pow>`         | 71.94 | 74.34 | 73.12 | | 81.48    | 55.58 | 66.00 | 0.1069 | 1773    |
| `<prefix>`      | 92.6  | 86.48 | 89.43 | | 67.56    | 92.17 | 77.79 | 0.0300 | 1287    |
| -----           |       |
| All (micro avg) | 80.39 | 81.12 | 80.76 | | 73.02    | 64.97 | 68.76 | 0.0167 | 6288    |

**TO BE RE-EVALUATED**

| Labels          | BidLSTM_CRF |       |       | BidLSTM_CRF_FEATURES |       |       |        | Support |
|-----------------|-------------|-------|-------|----------------------|-------|-------|--------|---------|
| Metrics         | P           | R     | F1    | P                    | R     | F1    | St.dev |         |
| `<base>`        | 56.01       | 50.34 | 53.02 | 59.98                | 56.33 | 58.09 | 0.0318 | 3228    |
| `<pow>`         | 93.70       | 62.38 | 74.88 | 93.71                | 68.40 | 78.94 | 0.1069 | 1773    |
| `<prefix>`      | 80.31       | 85.25 | 82.54 | 83.21                | 83.58 | 83.35 | 0.0300 | 1287    |
| -----           |
| All (micro avg) | 70.19       | 60.88 | 65.20 | 73.03                | 65.31 | 68.94 | 0.0167 | 6288    |

#### Values

| Labels          | CRF   |       |       | BERT_CRF |        |        |        |         |
|-----------------|-------|-------|-------|----------|--------|--------|--------|---------|
| Metrics         | P     | R     | F1    | P        | R      | F1     | St.dev | Support |
| `<alpha>`       | 96.9  | 99.21 | 98.04 | 99.21    | 99.37  | 99.29  | 0.0017 | 464     |   
| `<base>`        | 100   | 92.31 | 96    | 100.00   | 100.00 | 100.00 | 0.0000 | 13      |   
| `<number>`      | 99.14 | 99.63 | 99.38 | 99.43    | 99.46  | 99.44  | 0.0005 | 581     |   
| `<pow>`         | 100   | 100   | 100   | 100.00   | 100.00 | 100.00 | 0.0000 | 35      |
| -----           |
| All (micro avg) | 98.86 | 99.48 | 99.17 | 99.42    | 99.46  | 99.44  | 0.0004 | 1093    | 

| Labels          | BidLSTM_CRF |       |       |        |       | BidLSTM_CRF_FEATURES |       |       |        |         |
|-----------------|-------------|-------|-------|--------|-------|----------------------|-------|-------|--------|---------|
| Metrics         | P           | R     | F1    | St.dev |       | P                    | R     | F1    | St.dev | Support |
| `<alpha>`       | 97.82       | 99.53 | 98.66 | 0.0035 |       | 93.13                | 89.96 | 91.52 | 0.0086 | 464     |
| `<base>`        | 97.78       | 67.69 | 79.46 | 0.0937 |       | 23.67                | 40.00 | 29.70 | 0.0139 | 13      |
| `<number>`      | 98.92       | 99.33 | 99.13 | 0.0008 |       | 85.46                | 87.99 | 86.70 | 0.0041 | 581     |
| `<pow>`         | 69.11       | 73.85 | 71.29 | 0.1456 |       | 98.75                | 90.29 | 94.33 | 0.0163 | 35      |
| -----           |             |       |
| All (micro avg) | 98.34       | 98.59 | 98.47 | 0.0023 |       | 86.50                | 83.97 | 85.22 | 0.0031 | 1093    |

### Other published results

> :information_source: The paper \"Automatic Identification and Normalisation of Physical Measurements in Scientific
> Literature,\" published in September 2019, reported macro averaged evaluation scores.
