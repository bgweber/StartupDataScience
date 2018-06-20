

df <- read.csv("https://github.com/bgweber/Twitch/raw/master/Recommendations/Games.csv", header = F)
write.csv(df, "games.csv")

df <-read.csv("games.csv")
head(df)

library(sqldf)

sqldf("select V2, sum(1) from df group by 1 order by 1")

games <- sqldf("select 
   max(case when V2 = 1 then 1 else 0 end) as G1
  ,max(case when V2 = 2 then 1 else 0 end) as G2
  ,max(case when V2 = 3 then 1 else 0 end) as G3
  ,max(case when V2 = 4 then 1 else 0 end) as G4
  ,max(case when V2 = 5 then 1 else 0 end) as G5
  ,max(case when V2 = 6 then 1 else 0 end) as G6
  ,max(case when V2 = 7 then 1 else 0 end) as G7
  ,max(case when V2 = 8 then 1 else 0 end) as G8
  ,max(case when V2 = 9 then 1 else 0 end) as G9
  ,max(case when V2 = 10 then 1 else 0 end) as G10
  ,max(case when V2 = 1 then 1 else 0 end) as label
from df
group by V1
")

nrow(games)
train <- games[1:15000, ]
test <- games[15001:22900, ]

head(train)
head(test)

# load the data set

model <- keras_model_sequential() 
model %>% 
  layer_dense(units = 24, activation = 'relu', input_shape = c(10)) %>% 
  layer_dropout(rate = 0.1) %>%
  layer_dense(
    units              = 32, 
    kernel_initializer = "uniform", 
    activation         = "relu") %>%   
  layer_dropout(rate = 0.1) %>%
  layer_dense(
    units              = 32, 
    kernel_initializer = "uniform", 
    activation         = "relu") %>%   
  layer_dropout(rate = 0.1) %>%
  #layer_dense(units = 1, activation = 'softmax')
  layer_dense(units = 1, activation = "sigmoid")

iris.training <- normalize(as.matrix(train[,1:ncol(train) - 1]))
iris.trainLabels <-  train$label
#iris.trainLabels <-  to_categorical(train$label)

str(iris.trainLabels)

model %>% compile(
  optimizer = "rmsprop",
  loss = "binary_crossentropy",
  metrics = c("accuracy")
)

history <- model %>% fit(
  iris.training, 
  iris.trainLabels, 
  epochs = 500, 
  batch_size = 10, 
  validation_split = 0.2
)

plot(history)







# load the data set
library(keras)

# show histograms of the data sets
x <- (train_targets*1000)^2/2500000
hist(train_targets, main = "Original Prices", xlab = "Home Price")
hist(x, main = "Transformed Prices", xlab = "Home Price")


# The model as specified in "Deep Learning with R"
model <- keras_model_sequential() %>%
  layer_dense(units = 64, activation = "relu",
              input_shape = dim(train_data)[[2]]) %>%
  layer_dense(units = 64, activation = "relu") %>%
  layer_dense(units = 1)

# Compile the model, and select one of the loss functions
losses <- c(keras::loss_mean_squared_error, keras::loss_mean_squared_logarithmic_error, MLAE, MSLAE)

model %>% compile(
  optimizer = "rmsprop",
  loss = losses[1],
  metrics = c("mae")
)

# Train the model with validation
model %>% fit(
  train_data,
  train_targets,
  epochs = 100,
  batch_size = 5,
  verbose = 1,
  validation_split = 0.2
)

# Mode performance
results <- model %>% evaluate(test_data, test_targets, verbose = 0)
results$mean_absolute_error
