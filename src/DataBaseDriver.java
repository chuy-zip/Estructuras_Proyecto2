import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

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
                        "MERGE (p)-[:FAVORITE]->(j)", parameters("personaName", personaName, "juegoName", juegoName));
                return null;
            });
        }
    }
    public void deleteFavoriteConnection(String nombrePersona, String nombreJuego) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (p:Persona {nombre: $nombrePersona})-[f:FAVORITE]->(j:Juego {nombre: $nombreJuego}) " +
                        "DELETE f", parameters("nombrePersona", nombrePersona, "nombreJuego", nombreJuego));
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
                tx.run("CREATE (:Persona {nombre: $nombre, edad: $edad, password: $password, nintendo: $nintendo, pc: $pc, mobile: $mobile, xbox: $xbox, playstation: $playstation, preferMulti: $preferMulti})", parameters("nombre", nombre, "edad", edad, "password", password, "nintendo", nintendo, "pc", pc, "mobile", mobile, "xbox", xbox, "playstation", playstation, "preferMulti", preferMulti));
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
                tx.run("CREATE (:Juego {nombre: $nombre, descripcion: $descripcion,  nintendo: $nintendo, pc: $pc, mobile: $mobile, xbox: $xbox, playstation: $playstation, isMultiplayer: $isMultiplayer, ESRBRating: $ESRBRating})", parameters("nombre", nombreJuego, "descripcion", descripcion, "nintendo", nintendo, "pc", pc, "mobile", mobile, "xbox", xbox, "playstation", playstation, "isMultiplayer", isMultiplayer, "ESRBRating", ESRBRating));
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
                        "CREATE (p)-[:JUGADO]->(j)", parameters("nombrePersona", nombrePersona, "nombreJuego", nombreJuego));
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
            session.run(query, parameters("duracion", duracion));
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
            Result result = session.run(query, parameters("duracion", duracion));

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
            session.run(deleteQuery, parameters("nombreJuego", nombreJuego));

            // Crear la nueva relación entre el juego y la duración
            String createQuery = "MATCH (j:Juego {nombre: $nombreJuego}), (d:Duration {duracion: $duracion}) " +
                    "CREATE (j)-[:TIENE_DURACION]->(d)";
            session.run(createQuery, parameters("nombreJuego", nombreJuego, "duracion", duracion));
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
            Result result = session.run(existeQuery, parameters("categoria", categoria));

            if (result.hasNext() && result.next().get("count").asInt() == 0) {
                // Crear el nodo de categoría
                String crearCategoriaQuery = "CREATE (:Categoria {nombre: $categoria})";
                session.run(crearCategoriaQuery, parameters("categoria", categoria));
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
            session.run(crearRelacionQuery, parameters("nombreJuego", nombreJuego, "categoria", categoria));
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
            Result result = session.run(existeQuery, parameters("titulo", plataforma));

            if (result.hasNext() && result.next().get("count").asInt() == 0) {
                // Crear el nodo personalizado
                String crearNodoQuery = "CREATE (:Plataforma {titulo: $titulo, propiedad: $plataforma})";
                session.run(crearNodoQuery, parameters("titulo", plataforma, "plataforma", plataforma));
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
                session.run(crearRelacionQuery, parameters("nombreJuego", nombreJuego, "titulo", plataforma));
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
                            parameters("nombreJuego", nombreJuego));
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
                Result result = session.run(existeQuery, parameters("rating", rating));

                if (result.hasNext() && result.next().get("count").asInt() == 0) {
                    // Crear el nodo personalizado
                    String crearNodoQuery = "CREATE (:Rating {rating: $rating, rating: $rating})";
                    session.run(crearNodoQuery, parameters("rating", rating, "rating", rating));
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
            Result result = session.run(query, parameters("rating", rating));

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
                        "CREATE (j)-[:TIENE_RATING]->(r)", parameters("nombreJuego", nombreJuego, "rating", rating));
                return null;
            });
        }
    }
    public Game mapGame(Node gameNode) {
        String gameName = gameNode.get("nombre").asString();
        String description = gameNode.get("description").asString();
        boolean isOnNintendo = gameNode.get("nintendo").asBoolean();
        boolean isOnPC = gameNode.get("pc").asBoolean();
        boolean isOnMobile = gameNode.get("mobile").asBoolean();
        boolean isOnXbox = gameNode.get("xbox").asBoolean();
        boolean isOnPlayStation = gameNode.get("playstation").asBoolean();
        boolean isMultiplayer = gameNode.get("isMultiplayer").asBoolean();
        String esrbRating = gameNode.get("ESRBRating").asString();

        // Retrieve categories using Cypher query
        List<String> categories = new ArrayList<>();
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (j:Juego)-[:CATEGORIA]->(c:Categoria) WHERE j.nombre = $gameName RETURN c.nombre",
                    parameters("gameName", gameName));
            while (result.hasNext()) {
                Record record = result.next();
                categories.add(record.get("c.nombre").asString());
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve categories for game: " + gameName);
            e.printStackTrace();
        }

        // Retrieve duration using Cypher query
        String duration = null;
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (j:Juego)-[:TIENE_DURACION]->(d:Duration) WHERE j.nombre = $gameName RETURN d.duracion",
                    parameters("gameName", gameName));
            if (result.hasNext()) {
                Record record = result.next();
                duration = record.get("d.duracion").asString();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve duration for game: " + gameName);
            e.printStackTrace();
        }

        return new Game(gameName, description, duration, categories.get(0), categories.get(1), categories.get(2),
                isOnNintendo, isOnPC, isOnMobile, isOnXbox, isOnPlayStation, isMultiplayer, esrbRating);
    }

    public User mapUser(Node userNode) {
        String userName = userNode.get("nombre").asString();
        String userPassword = userNode.get("password").asString();
        int userAge = userNode.get("edad").asInt();
        boolean prefersMobile = userNode.get("mobile").asBoolean();
        boolean prefersNintendo = userNode.get("nintendo").asBoolean();
        boolean prefersPC = userNode.get("pc").asBoolean();
        boolean prefersXbox = userNode.get("xbox").asBoolean();
        boolean prefersPlaystation = userNode.get("playstation").asBoolean();
        boolean prefersMultiplayer = userNode.get("preferMulti").asBoolean();

        ArrayList<Game> playedGames = new ArrayList<>();
        ArrayList<Game> favoriteGames = new ArrayList<>();

        // Retrieve played games using Cypher query
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (user:Persona)-[:JUGADO]->(juego:Juego) WHERE user.nombre = $userName RETURN juego",
                    parameters("userName", userName));
            while (result.hasNext()) {
                Record record = result.next();
                Node gameNode = record.get("juego").asNode();
                Game game = mapGame(gameNode);
                playedGames.add(game);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve played games for user: " + userName);
            e.printStackTrace();
        }

        // Retrieve favorite games using Cypher query
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (user:Persona)-[:FAVORITE]->(juego:Juego) WHERE user.nombre = $userName RETURN juego",
                    parameters("userName", userName));
            while (result.hasNext()) {
                Record record = result.next();
                Node gameNode = record.get("juego").asNode();
                Game game = mapGame(gameNode);
                favoriteGames.add(game);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve favorite games for user: " + userName);
            e.printStackTrace();
        }

        return new User(userName, userPassword, userAge, prefersNintendo, prefersPC, prefersMobile,
                prefersXbox, prefersPlaystation, prefersMultiplayer, playedGames, favoriteGames);
    }
    public Node findUserNode(String userName, String password) {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (user:Persona {nombre: $userName, password: $password}) RETURN user",
                    parameters("userName", userName, "password", password));
            if (result.hasNext()) {
                Record record = result.next();
                return record.get("user").asNode();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to find user node for userName: " + userName + ", password: " + password);
            e.printStackTrace();
        }
        return null; // User node not found
    }



    public Node getGameNodeByName(String gameName) {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (j:Juego) WHERE j.nombre = $gameName RETURN j", parameters("gameName", gameName));
            if (result.hasNext()) {
                Record record = result.next();
                return record.get("j").asNode();
            } else {
                LOGGER.warning("Game node not found for name: " + gameName);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve game node for name: " + gameName);
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Game> getCompatibleGames(User user) {
        ArrayList<Game> compatibleGames = new ArrayList<>();

        try (Session session = driver.session()) {
            Result result = session.run("MATCH (u:Persona {nombre: $userName})\n" +
                            "MATCH (g:Juego)\n" +
                            "WHERE (g.nintendo OR g.pc OR g.mobile OR g.xbox OR g.playstation)\n" +
                            "AND (u.nintendo OR u.pc OR u.mobile OR u.xbox OR u.playstation)\n" +
                            "AND g.isMultiplayer = u.preferMulti\n" +
                            "RETURN g",
                    parameters("userName", user.getUserName()));

            while (result.hasNext()) {
                Record record = result.next();
                Node gameNode = record.get("g").asNode();
                Game game = mapGame(gameNode);
                compatibleGames.add(game);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve compatible games for user: " + user.getUserName());
            e.printStackTrace();
        }

        return compatibleGames;
    }
    public boolean userExists(String username, String password) {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (u:Persona {nombre: $username, password: $password}) RETURN count(u) AS count",
                    parameters("username", username, "password", password));

            if (result.hasNext()) {
                Record record = result.next();
                int count = record.get("count").asInt();
                return count > 0;
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to check user existence for username: " + username);
            e.printStackTrace();
        }
        return false;
    }

}