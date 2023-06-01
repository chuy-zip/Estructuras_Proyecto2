import os
from dotenv import load_dotenv
from neo4j import GraphDatabase
from flask import Flask,jsonify,request,render_template,redirect,url_for,session
from apps.DataBaseDriver import *

# Load environment variables from .env file
load_dotenv()



# Access environment variables
# neo4j_uri = os.getenv('NEO4J_URI')
# neo4j_user = os.getenv('NEO4J_USER')
# neo4j_password = os.getenv('NEO4J_PASSWORD')

# Create a driver object
# driver = GraphDatabase.driver(neo4j_uri, auth=(neo4j_user, neo4j_password))

# Create a Flask application
app = Flask(__name__)

# def verificar_sesion():
#     if 'usuario' not in session:
#         return True
#     pass

def init_var_session(usuario):
    session['user']

@app.route('/', methods=['GET', 'POST'])
def pagina_inicio():
    return render_template('pagina-inicial.html')
@app.route('/login', methods=['GET', 'POST'])
def login():
    return render_template('pages-login.html')
@app.route('/signup', methods=['GET', 'POST'])
def sigin():
    return render_template('pages-register.html')
@app.route('/dashboard', methods=['GET', 'POST'])
def index():
    return render_template('index.html')
@app.route('/users', methods=['GET', 'POST'])
def usuario():
    return render_template('users-profile.html')
@app.route('/dashboard/game', methods=['GET', 'POST'])
def videojuego():
    return render_template('pagina-videojuego.html')
# @app.route('/', methods=['GET', 'POST'])
# def login():
#     if request.method == 'GET':
#         return render_template('login.html')
#     if request.method == 'POST':
#         data = request.form
#         email = data.get("email")
#         pswd = data.get("pswd")

#         with driver.session() as session:
#             # Perform database operation
#             result = session.run("MATCH (u:User {email: $email, password: $pswd}) RETURN count(u) AS userCount",
#                                  email=email, pswd=pswd)
#             user_count = result.single()["userCount"]

#             if user_count == 1:
#                 session["email"] = email
#                 session["pswd"] = pswd
#                 # Fetch additional user data and set session variables
#                 # ...

#                 return redirect(url_for('home'))
#             else:
#                 return "Incorrect login credentials"
# # Connect to the database
# with driver.session() as session:
#     # Perform database operations
#     result = session.run("MATCH (n) RETURN count(n) AS nodeCount")
#     print(result.single()["nodeCount"])

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(debug=True, host='0.0.0.0', port=port) 

# Close the driver
# driver.close()