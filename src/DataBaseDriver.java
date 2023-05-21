import org.neo4j.driver.*;
import org.neo4j.driver.Session;

import java.util.logging.Logger;

public class DataBaseDriver implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(DataBaseDriver.class.getName());
    private final Driver driver;
    private final Session session;

    public DataBaseDriver(String uri, String user, String password, Config config) {
        // The driver is a long living object and should be opened during the start of your application
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
        this.session = driver.session();
    }

    @Override
    public void close() {
        // The driver object should be closed before the application ends.
        driver.close();
    }

    /**
     * Creates a relation between a user and a Game
     * @param personaName The ID of the user which is adding a game to its favorites list
     * @param juegoName The game to be connected as favorite
     */
    public void createFavoriteConnection(String personaName, String juegoName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (p:Persona {nombre: $personaName}), (j:Juego {nombre: $juegoName}) " +
                        "MERGE (p)-[:FAVORITE]->(j)", Values.parameters("personaName", personaName, "juegoName", juegoName));
                return null;
            });
        }
    }
    public void deleteFavoriteConnection(String nombrePersona, String nombreJuego) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (p:Persona {nombre: $nombrePersona})-[f:FAVORITE]->(j:Juego {nombre: $nombreJuego}) " +
                        "DELETE f", Values.parameters("nombrePersona", nombrePersona, "nombreJuego", nombreJuego));
                return null;
            });
        }
    }
    /**
     * Crea un nodo persona
     * @param nombre
     * @param edad
     * @param password
     */
    public void crearNodoPersona(String nombre, int edad, String password, boolean nintendo, boolean pc, boolean mobile, boolean xbox, boolean playstation, boolean preferMulti) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Persona {nombre: $nombre, edad: $edad, password: $password, nintendo: $nintendo, pc: $pc, mobile: $mobile, xbox: $xbox, playstation: $playstation, preferMulti: $preferMulti})", Values.parameters("nombre", nombre, "edad", edad, "password", password, "nintendo", nintendo, "pc", pc, "mobile", mobile, "xbox", xbox, "playstation", playstation, "preferMulti", preferMulti));
                return null;
            });
        }
    }
    /**
     * Crea un nodo juego
     * @param nombreJuego
     * @param descripcion
     */

    public void crearNodoJuego(String nombreJuego, String descripcion, boolean nintendo, boolean pc, boolean mobile, boolean xbox, boolean playstation, boolean isMultiplayer, String ESRBRating) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Juego {nombre: $nombre, descripcion: $descripcion,  nintendo: $nintendo, pc: $pc, mobile: $mobile, xbox: $xbox, playstation: $playstation, isMultiplayer: $isMultiplayer, ESRBRating: $ESRBRating})", Values.parameters("nombre", nombreJuego, "descripcion", descripcion, "nintendo", nintendo, "pc", pc, "mobile", mobile, "xbox", xbox, "playstation", playstation, "isMultiplayer", isMultiplayer, "ESRBRating", ESRBRating));
                return null;
            });
        }
    }


    /**
     * Crea relacion entre persona y un juego
     * @param nombrePersona
     * @param nombreJuego
     */
    public void crearRelacionPersonaJuego(String nombrePersona, String nombreJuego) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (p:Persona {nombre: $nombrePersona}), (j:Juego {nombre: $nombreJuego}) " +
                        "CREATE (p)-[:JUGADO]->(j)", Values.parameters("nombrePersona", nombrePersona, "nombreJuego", nombreJuego));
                return null;
            });
        }

    }


    /**
     * Crea un nodo duracion
     * @param duracion
     */
    public void crearNodoDuracion(String duracion) {
        try (Session session = driver.session()) {
            String query = "CREATE (:Duration {duracion: $duracion})";
            session.run(query, Values.parameters("duracion", duracion));
        }
    }


    /**
     * Verifica si ya existe el nodo de duracion especifica y de no ser así lo crea
     * @param duracion
     */
    public void crearNodosDuracion(String duracion) {
        // Verificar si ya existe un nodo de duración con el mismo valor
        if (existeNodoDuracion(duracion)) {
            return;
        }

        // Crear el nodo de duración
        crearNodoDuracion(duracion);
    }




    /**
     * devuelve true su el Nodo con una duracion especifica ya existe, false si no existe
     * @param duracion
     * @return
     */
    public boolean existeNodoDuracion(String duracion) {
        try (Session session = driver.session()) {
            String query = "MATCH (d:Duration {duracion: $duracion}) RETURN d";
            Result result = session.run(query, Values.parameters("duracion", duracion));

            return result.hasNext();
        }
    }

    /**
     * Crea una relacion entre el juego y su duracion
     * @param nombreJuego
     * @param duracion
     */
    public void crearRelacionJuegoDuracion(String nombreJuego, String duracion) {
        try (Session session = driver.session()) {
            // Eliminar relaciones existentes entre el juego y la duración
            String deleteQuery = "MATCH (j:Juego {nombre: $nombreJuego})-[r:TIENE_DURACION]->(d:Duration) DELETE r";
            session.run(deleteQuery, Values.parameters("nombreJuego", nombreJuego));

            // Crear la nueva relación entre el juego y la duración
            String createQuery = "MATCH (j:Juego {nombre: $nombreJuego}), (d:Duration {duracion: $duracion}) " +
                    "CREATE (j)-[:TIENE_DURACION]->(d)";
            session.run(createQuery, Values.parameters("nombreJuego", nombreJuego, "duracion", duracion));
        }
    }

    /**
     * Crea un Nodo de una categoria especifica de un juego
     * @param categoria
     */
    public void crearNodoCategoria(String categoria) {
        try (Session session = driver.session()) {
            // Verificar si el nodo de categoría ya existe
            String existeQuery = "MATCH (c:Categoria {nombre: $categoria}) RETURN count(c) AS count";
            Result result = session.run(existeQuery, Values.parameters("categoria", categoria));

            if (result.hasNext() && result.next().get("count").asInt() == 0) {
                // Crear el nodo de categoría
                String crearCategoriaQuery = "CREATE (:Categoria {nombre: $categoria})";
                session.run(crearCategoriaQuery, Values.parameters("categoria", categoria));
            }
        }
    }

    /**
     * Relaciona un juego con su categoria
     * @param nombreJuego
     * @param categoria
     */
    public void crearRelacionJuegoCategoria(String nombreJuego, String categoria) {
        try (Session session = driver.session()) {
            // Crear la relación entre el juego y la categoría
            String crearRelacionQuery = "MATCH (j:Juego {nombre: $nombreJuego}), (c:Categoria {nombre: $categoria}) " +
                    "CREATE (j)-[:CATEGORIA]->(c)";
            session.run(crearRelacionQuery, Values.parameters("nombreJuego", nombreJuego, "categoria", categoria));
        }
    }


    /**
     * Crea un Nodo de una plataforma (de gaming) especifica
     * @param plataforma
     */
    public void crearNodoPlataforma(String plataforma) {
        try (Session session = driver.session()) {
            // Verificar si el nodo personalizado ya existe
            String existeQuery = "MATCH (n:Plataforma {titulo: $titulo}) RETURN count(n) AS count";
            Result result = session.run(existeQuery, Values.parameters("titulo", plataforma));

            if (result.hasNext() && result.next().get("count").asInt() == 0) {
                // Crear el nodo personalizado
                String crearNodoQuery = "CREATE (:Plataforma {titulo: $titulo, propiedad: $plataforma})";
                session.run(crearNodoQuery, Values.parameters("titulo", plataforma, "plataforma", plataforma));
            }
        }
    }


    /**
     * Relaciona un juego con la plataforma para la que el juego esta disponible
     * @param nombreJuego
     * @param plataforma
     * @param valor
     */

    public void crearRelacionJuegoPlataforma(String nombreJuego, String plataforma, boolean valor) {
        if (valor) {
            try (Session session = driver.session()) {
                // Crear la relación entre el juego y el nodo personalizado
                String crearRelacionQuery = "MATCH (j:Juego {nombre: $nombreJuego}), (n:Personalizado {titulo: $titulo}) " +
                        "CREATE (j)-[:DISPONIBLE]->(n)";
                session.run(crearRelacionQuery, Values.parameters("nombreJuego", nombreJuego, "titulo", plataforma));
            }
        }
    }

    /**
     * Se asegura de que el nodo multiplayer no se vuelva a crear
     * @return
     */
    public boolean existeNodoMultiplayer() {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (m:Multiplayer) RETURN count(m) AS count");
            return result.hasNext() && result.next().get("count").asInt() > 0;
        }
    }

    /**
     * Crea un nodo con una unica propiedad Multiplayer.
     */
    public void crearNodoMultiplayer() {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Multiplayer {titulo: 'multiplayer'})");
                return null;
            });
        }
    }


    /**
     * Crea una relacion entre los juegos que tienen la opcion multiplayer
     * @param nombreJuego
     * @param multiplayer
     */
    public void crearRelacionJuegoMultiplayer(String nombreJuego, boolean multiplayer) {
        if (multiplayer) {
            try (Session session = driver.session()) {
                session.writeTransaction(tx -> {
                    tx.run("MATCH (j:Juego {nombre: $nombreJuego}), (m:Multiplayer {titulo: 'multiplayer'}) " +
                                    "CREATE (j)-[:MULTIPLAYER]->(m)",
                            Values.parameters("nombreJuego", nombreJuego));
                    return null;
                });
            }
        }
    }


    /**
     * Crea un nodo con un rating especifico del juego
     * @param rating
     */
    public void crearNodoRating(String rating) {
        if (existeNodoRating(rating) == false) {
            try (Session session = driver.session()) {
                // Verificar si el nodo personalizado ya existe
                String existeQuery = "MATCH (n:Rating {rating: $rating}) RETURN count(n) AS count";
                Result result = session.run(existeQuery, Values.parameters("rating", rating));

                if (result.hasNext() && result.next().get("count").asInt() == 0) {
                    // Crear el nodo personalizado
                    String crearNodoQuery = "CREATE (:Rating {rating: $rating, rating: $rating})";
                    session.run(crearNodoQuery, Values.parameters("rating", rating, "rating", rating));
                }
            }
        }
    }


    /**
     * Devuelve true si el nodo de ese rating especifico ya exsite
     * @param rating
     * @return
     */
    public boolean existeNodoRating(String rating) {
        try (Session session = driver.session()) {
            String query = "MATCH (d:Rating {rating: $rating}) RETURN d";
            Result result = session.run(query, Values.parameters("rating", rating));

            return result.hasNext();
        }
    }

    /**
     * Crea larelacion entre el juego y su rating
     * @param nombreJuego
     * @param rating
     */
    public void crearRelacionJuegoRating(String nombreJuego, String rating) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (j:Juego {nombre: $nombreJuego}), (r:Rating {rating: $rating}) " +
                        "CREATE (j)-[:TIENE_RATING]->(r)", Values.parameters("nombreJuego", nombreJuego, "rating", rating));
                return null;
            });
        }
    }




}