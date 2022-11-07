## General information

| set        | entities  | uniq_entities   |
|------------|-----------|-----------------|
| training   | 3913      | 1907            |
| holdout    | 1009      | 498             |
| ratio      | 25.79%    | 26.11%          |

## Labels information

| set        | `<number>` | `<alpha>` | `<time>` | `<base>` | `<pow>` |
|------------|------------|-----------|----------|----------|---------|
| training   | 2971       | 675       | 196      | 37       | 34      |
| holdout    | 811        | 126       | 46       | 13       | 13      |
| ratio      | 27.30%     | 18.67%    | 23.47%   | 35.14%   | 38.24%  |



## In-domain / out-of-domain information

| label       | # in domain | # in domain uniques | # out domain | # out domain unique |
|-------------|-------------|---------------------|--------------|---------------------|
| `<number>`  | 634         | 150                 | 177          | 129                 |
| `<alpha>`   | 113         | 14                  | 13           | 11                  |
| `<time>`    | 11          | 6                   | 35           | 25                  |
| `<base>`    | 13          | 1                   | 0            | 0                   |
| `<pow>`     | 12          | 5                   | 1            | 1                   |

**NOTE**: base and pow have low variability. Base has probably one single value (10) in all the examples, 
so it results difficult to obtain higher out of domain values. 
At the moment we don't have examples with `<exp>` in the training data. 
