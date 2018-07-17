import pandas as pd
from sklearn.externals import joblib
from sklearn.linear_model import LogisticRegression

df = pd.read_csv("https://github.com/bgweber/Twitch/raw/master/Recommendations/games-expand.csv")
y_train = df['label']
x_train = df.drop(['label'], axis=1)

model = LogisticRegression()
model.fit(x_train, y_train)

joblib.dump(model, 'logit.pkl')
