package itvinfierno;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Miriam
 */
public class Usuario {

    private static final String FICHERO = "usuarios.txt";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Leer usuarios
    public static Map<String, String> leerUsuarios() throws Exception {
        lock.readLock().lock();
        try {
            Map<String, String> mapa = new HashMap<>();

            File f = new File(FICHERO);
            if (!f.exists()) {
                return mapa;
            }

            BufferedReader br = new BufferedReader(new FileReader(f));
            String cifrado = br.readLine();
            br.close();

            if (cifrado == null) {
                return mapa;
            }

            String contenido = CifradoAES.descifrar(cifrado);

            for (String linea : contenido.split("\n")) {
                String[] partes = linea.split(":");
                if (partes.length == 2) {
                    mapa.put(partes[0], partes[1]);
                }
            }

            return mapa;

        } finally {
            lock.readLock().unlock();
        }
    }

    // Guardar usuarios
    public static void guardarUsuarios(Map<String, String> mapa) throws Exception {
        lock.writeLock().lock();
        try {
            StringBuilder sb = new StringBuilder();

            for (String email : mapa.keySet()) {
                sb.append(email).append(":").append(mapa.get(email)).append("\n");
            }

            String cifrado = CifradoAES.cifrar(sb.toString());

            PrintWriter pw = new PrintWriter(new FileWriter(FICHERO));
            pw.print(cifrado);
            pw.close();

        } finally {
            lock.writeLock().unlock();
        }
    }
}
