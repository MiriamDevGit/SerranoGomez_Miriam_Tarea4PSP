package itvinfierno;

//import com.sun.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author Miriam Serrano Gómez
 */
public class Servidor {

    private static final int PUERTO = 12349;

    public static void main(String[] args) throws Exception {
        System.out.println("Servidor ITV del Infierno arrancando...");

        int contador = 1;
        Itv itv = new Itv();

        // Cargar el almacén de claves 
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream keyFile = new FileInputStream("AlmacenSSL")) {
            keyStore.load(keyFile, "123456".toCharArray());
        }

        //Inicializar el gestor de claves con el keyStore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, "123456".toCharArray());

        //Inicializar el contexto SSL con el gestor de claves
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        // Declara objeto tipo Factory para crear socket SSL servidor
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();

        // Crea un socket servidor seguro
        SSLServerSocket socketServidorSsl = (SSLServerSocket) factory.createServerSocket(PUERTO);
        System.out.println("Servidor SSL escuchando en el puerto " + PUERTO);

        while (true) {
            SSLSocket socketSsl = (SSLSocket) socketServidorSsl.accept();
            HiloServidor hilo = new HiloServidor(socketSsl, itv);
            new Thread(hilo, "Coche" + (contador++)).start();
        }

    }
}
