
with records as (
  SELECT year, plurality, apgar_5min, mother_age, father_age,    
  gestation_weeks, ever_born, mother_married, weight_pounds
  FROM `bigquery-public-data.samples.natality`
  where year is not null and plurality is not null and apgar_5min is not null 
    and mother_age is not null and father_age is not null and gestation_weeks is not null
    and ever_born is not null and mother_married is not null and weight_pounds is not null
  order by rand() 
  LIMIT 10000 
)
, predictions as (
  select weight_pounds as actual,  
      11.82825946749738
    + year * -0.0015478882184680862
    + plurality * -2.1703912756511254
    + apgar_5min * -7.204416271249425E-4
    + mother_age * 0.011490472355621577
    + father_age * -0.0024906543152388157
    + gestation_weeks * 0.010845982465606988
    + ever_born * 0.010980856659668442
    + case when mother_married then 1 else 0 end * 0.26494217739205655
      as predicted
  from records
)
select sum(1) as records
  ,corr(actual, predicted) as Correlation
  ,avg(abs(actual - predicted)) as MAE
  ,avg(abs( (predicted - actual)/actual )) as Relative
from predictions 
