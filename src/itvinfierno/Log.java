package itvinfierno;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Miriam
 */
public class Log {

    private static final Logger logger = Logger.getLogger("MiLog");
    private static boolean inicializado = false;

    // Método para inicializar el logger
    public static void inicializar() {
        if (inicializado) {
            return;
        }

        try {
            FileHandler fh = new FileHandler("log.txt", true);

            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    String fechaHora = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    return String.format("(%s) %s%n", fechaHora, record.getMessage());
                }
            });

            logger.addHandler(fh);
            logger.setUseParentHandlers(false);

            inicializado = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para escribir en el log
    public static void escribir(String mensaje) {
        inicializar();
        logger.info(mensaje);
        //uso: logger.log(Level.INFO, "Login incorrecto: " + email);
    }
}
