import com.opencsv.exceptions.CsvValidationException;
import org.neo4j.driver.Config;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GeneratorDateBase {
    public static void main(String[] args) {
        String URI = System.getenv("NEO4J_URI");
        String USER = System.getenv("NEO4J_USERNAME");
        String PASSWORD = System.getenv("NEO4J_PASSWORD");

        System.out.println(URI);
        System.out.println(USER);
        System.out.println(PASSWORD);

        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            System.out.println("Connected");

            String csvPersons = "C:\\Users\\USUARIO\\Desktop\\Personas.csv";
            String csvGames= "C:\\Users\\USUARIO\\Desktop\\Juegos.csv";

            try (CSVReader readerGames = new CSVReader(Files.newBufferedReader(Paths.get(csvGames)))) {
                String[] encabezado = readerGames.readNext(); // Saltar la fila de encabezado

                String[] fila;
                while ((fila = readerGames.readNext()) != null) {
                    int i = 0;
                    String nombreJuego = fila[0];
                    String descripcion = fila[1];
                    String duration = fila[2];
                    String category1 = fila[3];
                    String category2 = fila[4];
                    String category3 = fila[5];
                    boolean nintendo = Boolean.parseBoolean(fila[6]);
                    boolean pc = Boolean.parseBoolean(fila[7]);
                    boolean mobile = Boolean.parseBoolean(fila[8]);
                    boolean xbox = Boolean.parseBoolean(fila[9]);
                    boolean playstation = Boolean.parseBoolean(fila[10]);
                    boolean multiplayer = Boolean.parseBoolean(fila[11]);
                    String rating =fila[12];


                    // Crear el nodo de juego
                    app.crearNodoJuego(nombreJuego, descripcion);

                    //Crear nodo duration
                    if (app.existeNodoDuracion(duration) == false){
                        app.crearNodosDuracion(duration);
                    }

                    //Crear relacion entre la duracion y el juego
                    app.crearRelacionJuegoDuracion(nombreJuego, duration);

                    //Crear nodos de catagoria1
                    app.crearNodoCategoria(category1);
                    app.crearRelacionJuegoCategoria(nombreJuego,category1);

                    //Crear nodos de catagoria2
                    app.crearNodoCategoria(category2);
                    app.crearRelacionJuegoCategoria(nombreJuego,category2);

                    //Crear nodos de catagoria3
                    app.crearNodoCategoria(category3);
                    app.crearRelacionJuegoCategoria(nombreJuego,category3);

                    //crear nodos de plataforma y nodo Multiplayer
                    if (i == 0){
                        app.crearNodoPlataforma("nintendo");
                        app.crearNodoPlataforma("pc");
                        app.crearNodoPlataforma("xbox");
                        app.crearNodoPlataforma("mobile");
                        app.crearNodoPlataforma("playstation");



                    }

                    //crea el nodo multiplayer
                    if (app.existeNodoMultiplayer() == false) {
                        app.crearNodoMultiplayer();
                    }



                    //crear relaciones Juego-Plataforma
                    app.crearRelacionJuegoPlataforma(nombreJuego,"nintendo",nintendo);
                    app.crearRelacionJuegoPlataforma(nombreJuego,"pc",pc);
                    app.crearRelacionJuegoPlataforma(nombreJuego,"xbox",xbox);
                    app.crearRelacionJuegoPlataforma(nombreJuego,"mobile",mobile);
                    app.crearRelacionJuegoPlataforma(nombreJuego,"playstation",playstation);

                    //Crear relacion Juego-Multiplayer
                    app.crearRelacionJuegoMultiplayer(nombreJuego,multiplayer);


                    //Crear nodo Rating
                    app.crearNodoRating(rating);

                    //Crear relacion Juego-Rating
                    app.crearRelacionJuegoRating(nombreJuego,rating);










                }
            }

            try (CSVReader readerPersons = new CSVReader(Files.newBufferedReader(Paths.get(csvPersons)))) {
                String[] encabezado = readerPersons.readNext(); // Saltar la fila de encabezado

                String[] fila;
                while ((fila = readerPersons.readNext()) != null) {
                    String nombre = fila[0];
                    int edad = Integer.parseInt(fila[1]);
                    String juego1 = fila[8];
                    String juego2 = fila[9];
                    String juego3 = fila[10];
                    String juego4 = fila[11];
                    String juego5 = fila[12];
                    String clave = fila[13];


                    String consola = fila[4];

                    // Crear el nodo de persona
                    app.crearNodoPersona(nombre, edad, clave);

                    // Crear la relación con el juego 1
                    app.crearRelacionPersonaJuego(nombre, juego1);

                    // Crear la relación con el juego 2
                    app.crearRelacionPersonaJuego(nombre, juego2);

                    // Crear la relación con el juego 3
                    app.crearRelacionPersonaJuego(nombre, juego3);

                    // Crear la relación con el juego 4
                    app.crearRelacionPersonaJuego(nombre, juego4);

                    // Crear la relación con el juego 5
                    app.crearRelacionPersonaJuego(nombre, juego5);

                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
