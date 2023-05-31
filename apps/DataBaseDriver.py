import os
from dotenv import load_dotenv
from neo4j import GraphDatabase

load_dotenv('./.env')

neo4j_uri = os.getenv('NEO4J_URI')
neo4j_user = os.getenv('NEO4J_USER')
neo4j_password = os.getenv('NEO4J_PASSWORD')

driver = GraphDatabase.driver(neo4j_uri, auth=(neo4j_user, neo4j_password))

#create a favorite connection
def create_favorite_connection(persona_name, juego_name):
    with driver.session() as session:
        session.write_transaction(lambda tx: create_relationship(tx, persona_name, juego_name))

def create_relationship(tx, persona_name, juego_name):
    query = "MATCH (p:Persona {nombre: $personaName}), (j:Juego {nombre: $juegoName}) " \
            "MERGE (p)-[:FAVORITE]->(j)"
    tx.run(query, personaName=persona_name, juegoName=juego_name)

# delete a favorite connection
def delete_favorite_connection(nombre_persona, nombre_juego):
    with driver.session() as session:
        session.write_transaction(lambda tx: delete_relationship(tx, nombre_persona, nombre_juego))

def delete_relationship(tx, nombre_persona, nombre_juego):
    query = "MATCH (p:Persona {nombre: $nombrePersona})-[f:FAVORITE]->(j:Juego {nombre: $nombreJuego}) " \
            "DELETE f"
    tx.run(query, nombrePersona=nombre_persona, nombreJuego=nombre_juego)

#create a node persona and inserting it to the database neo4j
def create_persona_node(nombre, edad, password, nintendo, pc, mobile, xbox, playstation, prefer_multi):
    with driver.session() as session:
        session.write_transaction(lambda tx: create_persona(tx, nombre, edad, password, nintendo, pc, mobile, xbox, playstation, prefer_multi))

def create_persona(tx, nombre, edad, password, nintendo, pc, mobile, xbox, playstation, prefer_multi):
    query = """
    CREATE (:Persona {nombre: $nombre, edad: $edad, password: $password, nintendo: $nintendo, pc: $pc, mobile: $mobile, xbox: $xbox, playstation: $playstation, preferMulti: $preferMulti})
    """
    tx.run(query, nombre=nombre, edad=edad, password=password, nintendo=nintendo, pc=pc, mobile=mobile, xbox=xbox, playstation=playstation, preferMulti=prefer_multi)

#create a node juego and inserting it to the database
def create_juego_node(nombre_juego, descripcion, nintendo, pc, mobile, xbox, playstation, is_multiplayer, esrb_rating):
    with driver.session() as session:
        session.write_transaction(lambda tx: create_juego(tx, nombre_juego, descripcion, nintendo, pc, mobile, xbox, playstation, is_multiplayer, esrb_rating))

def create_juego(tx, nombre_juego, descripcion, nintendo, pc, mobile, xbox, playstation, is_multiplayer, esrb_rating):
    query = """
    CREATE (:Juego {nombre: $nombre, descripcion: $descripcion, nintendo: $nintendo, pc: $pc, mobile: $mobile, xbox: $xbox, playstation: $playstation, isMultiplayer: $isMultiplayer, ESRBRating: $ESRBRating})
    """
    tx.run(query, nombre=nombre_juego, descripcion=descripcion, nintendo=nintendo, pc=pc, mobile=mobile, xbox=xbox, playstation=playstation, isMultiplayer=is_multiplayer, ESRBRating=esrb_rating)

#create person and game relation
def create_persona_juego_relationship(nombre_persona, nombre_juego):
    with driver.session() as session:
        session.write_transaction(lambda tx: create_relationship(tx, nombre_persona, nombre_juego))

def create_relationship(tx, nombre_persona, nombre_juego):
    query = """
    MATCH (p:Persona {nombre: $nombrePersona}), (j:Juego {nombre: $nombreJuego})
    CREATE (p)-[:JUGADO]->(j)
    """
    tx.run(query, nombrePersona=nombre_persona, nombreJuego=nombre_juego)

#create node duracion and inserting it into the database
def crear_nodo_duracion(duracion):
    with driver.session() as session:
        query = "CREATE (:Duration {duracion: $duracion})"
        session.run(query, duracion=duracion)

#verifies if node duration already exists
def existe_nodo_duracion(duracion):
    with driver.session() as session:
        query = "MATCH (d:Duration {duracion: $duracion}) RETURN d"
        result = session.run(query, duracion=duracion)

        return result.single() is not None

#creates relationshin between game and duration
def crear_relacion_juego_duracion(nombre_juego, duracion):
    with driver.session() as session:
        # Delete existing relationships between the juego and duracion
        delete_query = "MATCH (j:Juego {nombre: $nombreJuego})-[r:TIENE_DURACION]->(d:Duration) DELETE r"
        session.run(delete_query, nombreJuego=nombre_juego)

        # Create the new relationship between juego and duracion
        create_query = "MATCH (j:Juego {nombre: $nombreJuego}), (d:Duration {duracion: $duracion}) " \
                       "CREATE (j)-[:TIENE_DURACION]->(d)"
        session.run(create_query, nombreJuego=nombre_juego, duracion=duracion)

#creates a node for the category of the game
def crear_nodo_categoria(categoria):
    with driver.session() as session:
        # Check if the categoria node already exists
        existe_query = "MATCH (c:Categoria {nombre: $categoria}) RETURN count(c) AS count"
        result = session.run(existe_query, categoria=categoria)

        if result.single()["count"] == 0:
            # Create the categoria node
            crear_categoria_query = "CREATE (:Categoria {nombre: $categoria})"
            session.run(crear_categoria_query, categoria=categoria)

#creates relationship between game a category
def crear_relacion_juego_categoria(nombre_juego, categoria):
    with driver.session() as session:
        # Create the relationship between the juego and categoria
        crear_relacion_query = "MATCH (j:Juego {nombre: $nombreJuego}), (c:Categoria {nombre: $categoria}) " \
                               "CREATE (j)-[:CATEGORIA]->(c)"
        session.run(crear_relacion_query, nombreJuego=nombre_juego, categoria=categoria)

#creates node platform
def crear_nodo_plataforma(plataforma):
    with driver.session() as session:
        existe_query = "MATCH (n:Plataforma {titulo: $titulo}) RETURN count(n) AS count"
        result = session.run(existe_query, titulo=plataforma)

        if result.single()["count"] == 0:
            crear_nodo_query = "CREATE (:Plataforma {titulo: $titulo, propiedad: $plataforma})"
            session.run(crear_nodo_query, titulo=plataforma, plataforma=plataforma)

# creates relationship between game and platform
def crear_relacion_juego_plataforma(nombre_juego, plataforma, valor):
    if valor:
        with driver.session() as session:
            crear_relacion_query = "MATCH (j:Juego {nombre: $nombreJuego}), (n:Personalizado {titulo: $titulo}) " \
                                   "CREATE (j)-[:DISPONIBLE]->(n)"
            session.run(crear_relacion_query, nombreJuego=nombre_juego, titulo=plataforma)

#verifies if node multiplayer exist
#returns a boolean
def existe_nodo_multiplayer():
    with driver.session() as session:
        result = session.run("MATCH (m:Multiplayer) RETURN count(m) AS count")
        return result.single()["count"] > 0

#creates nodo multiplayer
def crear_nodo_multiplayer():
    with driver.session() as session:
        session.write_transaction(lambda tx: tx.run("CREATE (:Multiplayer {titulo: 'multiplayer'})"))

#creates relationship between game and multiplayer
# multiplayer has to be a boolean
def crear_relacion_juego_multiplayer(nombre_juego, multiplayer):
    if multiplayer:
        with driver.session() as session:
            session.write_transaction(lambda tx: tx.run("MATCH (j:Juego {nombre: $nombreJuego}), (m:Multiplayer {titulo: 'multiplayer'}) "
                                                       "CREATE (j)-[:MULTIPLAYER]->(m)",
                                                       nombreJuego=nombre_juego))

#verifies if node rating exists
def existe_nodo_rating(rating):
    with driver.session() as session:
        result = session.run("MATCH (d:Rating {rating: $rating}) RETURN d", rating=rating)
        return result.hasNext()

#creates node rating in the data base
def crear_nodo_rating(rating):
    if not existe_nodo_rating(rating):
        with driver.session() as session:
            session.write_transaction(lambda tx: tx.run("CREATE (:Rating {rating: $rating})", rating=rating))

#creates relationship between game and rating
def crear_relacion_juego_rating(nombre_juego, rating):
    with driver.session() as session:
        session.write_transaction(lambda tx: tx.run("MATCH (j:Juego {nombre: $nombreJuego}), (r:Rating {rating: $rating}) "
                                                   "CREATE (j)-[:TIENE_RATING]->(r)",
                                                   nombreJuego=nombre_juego, rating=rating))


with driver.session() as session:
    # Perform database operations
    result = session.run("MATCH (n) RETURN count(n) AS nodeCount")
    print(result.single()["nodeCount"])