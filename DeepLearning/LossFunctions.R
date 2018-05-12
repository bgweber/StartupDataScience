
# load the data set
library(keras)
dataset <- dataset_boston_housing()
c(c(train_data, train_targets), c(test_data, test_targets)) %<-% dataset

# show histograms of the data sets
x <- (train_targets*1000)^2/2500000
hist(train_targets, main = "Original Prices", xlab = "Home Price")
hist(x, main = "Transformed Prices", xlab = "Home Price")

# transform the training and test labels
train_targets <- (train_targets*1000)^2/2500000
test_targets <- (test_targets*1000)^2/2500000

# Mean Log Absolute Error
MLAE <- function( y_true, y_pred ) {
  K <- backend()
  K$mean( K$abs( K$log( K$relu(y_true *1000 ) + 1 ) - K$log( K$relu(y_pred*1000 ) + 1)))
}

# Mean Squared Log Absolute Error
MSLAE <- function( y_true, y_pred ) {
  K <- backend()
  K$mean( K$pow( K$abs( K$log( K$relu(y_true *1000 ) + 1 ) - K$log( K$relu(y_pred*1000 ) + 1)), 2))
}

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
