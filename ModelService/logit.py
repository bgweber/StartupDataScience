from sklearn.externals import joblib
import pandas as pd

model = joblib.load('logit.pkl') 

def lambda_handler(event, context):    
    p = event['queryStringParameters']
    print("Event params: " + str(p))
    x = pd.DataFrame.from_dict(p, orient='index').transpose()
    pred = model.predict_proba(x)[0][1]

    result = 'Prediction ' + str(pred)
    return {  "body": result } 
