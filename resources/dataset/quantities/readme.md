## General information

`positive examples` indicate the number of paragraphs with at least one entity while `negative examples' indicate the number of paragraphs without any entity.  

| set      | files   | sentences | tokens  | entities | uniq_entities | positive_examples | negative_examples |
|----------|---------|-----------|---------|----------|---------------|-------------------|-------------------|
| training | 26      | 9695      | 293884  | 3559     | 2338          | 3559              | 6136              |
| holdout  | 9       | 2097      | 67554   | 805      | 555           | 805               | 1292              |
| ratio    | 34.62%  | 21.63%    | 22.99%  | 22.62%   | 23.74%        | 22.62%            | 21.06%            |


## Labels information

This section provide metrics at label information. 
This section allow to grasp which labels are not evenly represented in both training or evaluation set. 

| set               | `<value>`   | `<interval>`  | `<list>` |
|-------------------|-------------|---------------|----------|
| training          | 2482        | 967           | 110      |
| holdout           | 590         | 191           | 24       |
| ratio             | 23.77%      | 19.75%        | 21.82%   |
| ---               |
| training unique   | 1232        | 632           | 102      |
| holdout unique    | 346         | 133           | 24       |
| ratio unique      | 28.08%      | 21.04%        | 23.53%   |



## In-domain / out-of-domain information

`Out-of-domain` information represent the entities that are occurring in the evaluation but not in the training dataset. 
They provide a degree on which the evaluation dataset lean toward generalisation (the highest rate of off-domain unique).

| label         | # in domain | # in domain unique | # out domain | # out domain unique |
|---------------|-------------|--------------------|--------------|---------------------|
| `<value>`     | 268         | 99                 | 322          | 247                 |
| `<interval>`  | 51          | 18                 | 140          | 115                 |
| `<list>`      | 1           | 1                  | 23           | 23                  |
