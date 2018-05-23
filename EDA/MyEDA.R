
# summary stats?
library(bigrquery)
project <- "your_project_id"

sql <- "
 SELECT year, plurality, mother_age, father_age,    
       gestation_weeks, ever_born, mother_married, weight_pounds
 FROM `bigquery-public-data.samples.natality`
 order by rand() 
 LIMIT 100000 "

df <- query_exec(sql, project = project, use_legacy_sql = FALSE)
summary(df[, 2:5])

# how common are twins? 
library(sqldf)
df <- df[!is.na(df$plurality), ]
sqldf("select plurality, sum(1) as Babies, sum(1)/97330.0 as Pct 
from df group by 1
order by 1")

# for later section (not too useful)
hist(df$plurality)

# histogram of birth weights 
str(df)
hist(df$weight_pounds, main = "Distribution of Birth Weights", xlab = "Weight")

# change the bucket size 
hist(df$weight_pounds, main = "Distribution of Birth Weights", xlab = "Weight", breaks = 100)

# CDFs!
plot(ecdf(df$weight_pounds), main = "CDF of Birth Weights", xlab = "Weight", ylab = "CDF")

# impact of transformations 
str(df)
hist(df$gestation_weeks, main = "Gestation Weeks", xlab = "Weeks")
hist(log(df$gestation_weeks), main = "Gestation Weeks (Log Transform)", xlab = "log(Weeks)")


# box plots 
str(df)
sample <- df[df$ever_born <= 6, ]
sample$ever_born <- sample$ever_born - 1 
boxplot(weight_pounds~ever_born,data=sample, main="Birth Weight vs Prior Births", 
        xlab="Prior Children Delivered", ylab="Birth Weight")


# scatter plots
sample <- df[1:10000, ]
sample <- sample[sample$gestation_weeks < 90, ]
plot(sample$gestation_weeks, sample$weight_pounds, main = "Birth Weight vs Gestation Weeks",
      xlab= " Gestation Weeks", ylab = "Birth Weight")


# correlation matrix 
res <- cor(df, method = "pearson", use = "complete.obs")
res
res <- ifelse(res > 0, res^.5, -abs(res)^.5)
library(corrplot)
corrplot(res, type = "upper", order = "hclust", tl.col = "black")


# create a linear regression model and plot feature importance 
library(relaimpo)
fit <- lm(weight_pounds ~ . , data = df)
boot <- boot.relimp(fit, b = 10, type = c("lmg", "first", "last", "pratt") 
                    , rank = TRUE, diff = TRUE, rela = TRUE)
booteval.relimp(boot) 
plot(booteval.relimp(boot,sort=TRUE)) 
