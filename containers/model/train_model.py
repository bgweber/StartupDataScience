# import panda, keras and tensorflow
import pandas as pd
import tensorflow as tf
import keras
from keras import models, layers

# Load the sample data set and split into x and y data frames 
df = pd.read_csv("https://github.com/bgweber/Twitch/raw/master/Recommendations/games-expand.csv")
x = df.drop(['label'], axis=1)
y = df['label']

# Define the keras model
model = models.Sequential()
model.add(layers.Dense(64, activation='relu', input_shape=(10,)))
model.add(layers.Dropout(0.1))
model.add(layers.Dense(64, activation='relu'))
model.add(layers.Dropout(0.1))
model.add(layers.Dense(64, activation='relu'))
model.add(layers.Dense(1, activation='sigmoid'))

# Use a custom metricfunction
def auc(y_true, y_pred):
    auc = tf.metrics.auc(y_true, y_pred)[1]
    keras.backend.get_session().run(tf.local_variables_initializer())
    return auc    

# Compile and fit the model
model.compile(optimizer='rmsprop',loss='binary_crossentropy', metrics=[auc])
history = model.fit(x, y, epochs=100, batch_size=100,  validation_split = .2, verbose=0)

# Save the model in h5 format 
model.save("games.h5")
