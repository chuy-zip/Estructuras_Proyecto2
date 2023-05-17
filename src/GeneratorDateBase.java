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
                    String nombreJuego = fila[0];
                    String descripcion = fila[1];

                    // Crear el nodo de juego
                    app.crearNodoJuego(nombreJuego, descripcion);
                }
            }

            try (CSVReader readerPersons = new CSVReader(Files.newBufferedReader(Paths.get(csvPersons)))) {
                String[] encabezado = readerPersons.readNext(); // Saltar la fila de encabezado

                String[] fila;
                while ((fila = readerPersons.readNext()) != null) {
                    String nombre = fila[0];
                    int edad = Integer.parseInt(fila[1]);
                    String juego1 = fila[2];
                    String juego2 = fila[3];
                    String consola = fila[4];

                    // Crear el nodo de persona
                    app.crearNodoPersona(nombre, edad);

                    // Crear la relación con el juego 1
                    app.crearRelacionPersonaJuego(nombre, juego1);

                    // Crear la relación con el juego 2
                    app.crearRelacionPersonaJuego(nombre, juego2);




                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
