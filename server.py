from flask import Flask,jsonify

app = Flask(__name__)

@app.rout('/')
def hello():
	return 'Hello world'

if __name__ == '__main__':
	app.run(debug=True)