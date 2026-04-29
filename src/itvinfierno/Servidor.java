package itvinfierno;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Miriam Serrano Gómez
 */
public class Servidor {

    private static final int PUERTO = 12349;

    public static void main(String[] args) {
        System.out.println("Servidor ITV del Infierno arrancando...");

        int contador = 1;
        Itv itv = new Itv();

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            while (true) {
                Socket socketCliente = servidor.accept();
                HiloServidor hilo = new HiloServidor(socketCliente, itv);
                new Thread(hilo, "Coche" + (contador++)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
