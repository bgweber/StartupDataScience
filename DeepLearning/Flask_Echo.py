import flask

# instantiate flask 
app = flask.Flask(__name__)

# define a predict function as an endpoint 
@app.route("/predict", methods=["GET","POST"])
def predict():
    data = {"success": False}

    params = flask.request.json
    if (params == None):
        params = flask.request.args

    # if parameters are found, echo the msg parameter 
    if (params != None):
        data["response"] = params.get("msg")
        data["success"] = True

    # return a reponse in json format 
    return flask.jsonify(data)    

# start the flask app, allow remote connections 
app.run(host='0.0.0.0')
