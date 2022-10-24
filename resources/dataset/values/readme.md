| set      | entities | uniq_entities | classes  | materials |
|----------|----------|---------------|----------|-----------|
| training | 3913     | 1907          | 5        | 3866      |
| holdout  | 1009     | 498           | 5        | 989       |
| ratio    | 25.79%   | 26.11%        | 100.00%  | 25.58%    |



| set      | number  | alpha   | time    | base    | pow     |
|----------|---------|---------|---------|---------|---------|
| training | 2971    | 675     | 196     | 37      | 34      |
| holdout  | 811     | 126     | 46      | 13      | 13      |
| ratio    | 27.30%  | 18.67%  | 23.47%  | 35.14%  | 38.24%  |



Out of domain

| label  | # in domain | # in domain uniques | # out domain | # out domain unique |
|--------|-------------|---------------------|--------------|---------------------|
| number | 634         | 150                 | 177          | 129                 |
| alpha  | 113         | 14                  | 13           | 11                  |
| time   | 11          | 6                   | 35           | 25                  |
| base   | 13          | 1                   | 0            | 0                   |
| pow    | 12          | 5                   | 1            | 1                   |

**NOTE**: base and pow have low variability. Base has probably one single value (10) in all the examples, 
so it results difficult to obtain higher out of domain values. 
At the moment we don't have examples with <exp> in the training data. 
