from flask import Flask, jsonify, request

app = Flask(__name__)

commands = ['light on', ]

@app.route('/')
def hello():
	return 'Hello world'
@app.route('/test')
def test():
	return jsonify(**{'status': 1,'result': ['val1','val2']})

@app.route('/command',methods=['POST'])
def do_command():
	command = request.form['command']
	#do some shit with command

if __name__ == '__main__':
	app.run(debug=True)