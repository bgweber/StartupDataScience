import pandas as pd
import tensorflow as tf
import keras
from keras import models, layers
keras.__version__

df = pd.read_csv("https://github.com/bgweber/Twitch/raw/master/Recommendations/games-expand.csv")
df.to_csv("games.csv", index=False)
df.head()

train = df[5000:]
test = df[:5000]

x = train.drop(['label'], axis=1)
y = train['label']

model = models.Sequential()
model.add(layers.Dense(64, activation='relu', input_shape=(10,)))
model.add(layers.Dropout(0.1))
model.add(layers.Dense(64, activation='relu'))
model.add(layers.Dropout(0.1))
model.add(layers.Dense(64, activation='relu'))
model.add(layers.Dense(1, activation='sigmoid'))


def auc(y_true, y_pred):
    auc = tf.metrics.auc(y_true, y_pred)[1]
    keras.backend.get_session().run(tf.local_variables_initializer())
    return auc
    
model.compile(optimizer='rmsprop',
              loss='binary_crossentropy',
              metrics=[auc])


history = model.fit(x,
                    y,
                    epochs=100,
                    batch_size=100,
                    validation_split = .2,
                    verbose=0)

x_test = test.drop(['label'], axis=1)
y_test = test['label']

results = model.evaluate(x_test, y_test, verbose = 0)
print(results)

model.save("games.h5")
