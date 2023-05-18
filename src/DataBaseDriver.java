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

    public void createGame(String name, String hours, String description) {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            Transaction tx = session.beginTransaction();
            String query = "CREATE (n:Game {Name: $name, Hours: $hours, Description: $description})";
            tx.run(query, Values.parameters("name", name, "hours", hours, "description", description));
            tx.commit();
        }
    }

    public void deleteGame(String name) {
        try (Session session = driver.session()) {
            session.writeTransaction(transaction -> {
                String query = "MATCH (n:Game {Name: $name}) DELETE n";
                Result result = transaction.run(query, Values.parameters("name", name));
                return null;
            });
            LOGGER.info("Game node with name " + name + " deleted successfully.");
        } catch (Exception e) {
            LOGGER.severe("Error deleting game node: " + e.getMessage());
        }
    }


    ///QUERY'S PARA CREAR NODOS///

    public void crearNodoPersona(String nombre, int edad) {
        String query = "CREATE (:Persona {nombre: $nombre, edad: $edad, name: $nombre})";
        session.run(query, Values.parameters("nombre", nombre, "edad", edad));
    }

    public void crearNodoJuego(String nombreJuego, String descripcion) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("CREATE (:Juego {nombre: $nombre, descripcion: $descripcion, name: $nombre})", Values.parameters("nombre", nombreJuego, "descripcion", descripcion));
                return null;
            });
        }
    }

    // Método para verificar si existe un nodo de duración
    public boolean existeNodoDuracion(String duracion) {
        try (Session session = driver.session()) {
            String query = "MATCH (d:Duration {duracion: $duracion}) RETURN d";
            Result result = session.run(query, Values.parameters("duracion", duracion));

            return result.hasNext();
        }
    }

    // Método para crear un nodo de duración
    public void crearNodoDuracion(String duracion) {
        try (Session session = driver.session()) {
            String query = "CREATE (:Duration {duracion: $duracion})";
            session.run(query, Values.parameters("duracion", duracion));
        }
    }





    // Método para crear una relación entre un juego y una duración
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


    public void crearNodosDuracion(String duracion) {
        // Verificar si ya existe un nodo de duración con el mismo valor
        if (existeNodoDuracion(duracion)) {
            return;
        }

        // Crear el nodo de duración
        crearNodoDuracion(duracion);
    }


    public void crearNodoPlataforma(String plataforma) {
        try (Session session = driver.session()) {
            // Verificar si el nodo personalizado ya existe
            String existeQuery = "MATCH (n:Personalizado {titulo: $titulo}) RETURN count(n) AS count";
            Result result = session.run(existeQuery, Values.parameters("titulo", plataforma));

            if (result.hasNext() && result.next().get("count").asInt() == 0) {
                // Crear el nodo personalizado
                String crearNodoQuery = "CREATE (:Personalizado {titulo: $titulo, propiedad: $plataforma})";
                session.run(crearNodoQuery, Values.parameters("titulo", plataforma, "plataforma", plataforma));
            }
        }
    }

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



















    public void crearRelacionJuegoCategoria(String nombreJuego, String categoria) {
        try (Session session = driver.session()) {
            // Crear la relación entre el juego y la categoría
            String crearRelacionQuery = "MATCH (j:Juego {nombre: $nombreJuego}), (c:Categoria {nombre: $categoria}) " +
                    "CREATE (j)-[:CATEGORIA]->(c)";
            session.run(crearRelacionQuery, Values.parameters("nombreJuego", nombreJuego, "categoria", categoria));
        }
    }






    public void crearRelacionPersonaJuego(String nombrePersona, String nombreJuego) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (p:Persona {nombre: $nombrePersona}), (j:Juego {nombre: $nombreJuego}) " +
                        "CREATE (p)-[:JUGADO]->(j)", Values.parameters("nombrePersona", nombrePersona, "nombreJuego", nombreJuego));
                return null;
            });
        }
    }



}