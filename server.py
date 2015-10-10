from flask import Flask,jsonify

app = Flask(__name__)

@app.route('/')
def hello():
	return 'Hello world'
@app.route('/test')
def test():
	return jsonify(**{'status': 1,'result': ['val1','val2']})

if __name__ == '__main__':
	app.run(debug=True)