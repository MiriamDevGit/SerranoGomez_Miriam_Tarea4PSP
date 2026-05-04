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

    
    public static Map<String, String> leerUsuarios() throws Exception {
        // Pueden leer, pero no se puede escribir mientras se lee
        lock.readLock().lock();

        try {
            // Crea el mapa donde se almacenarán los usuarios 
            Map<String, String> mapa = new HashMap<>();
            
            // Se obtiene el fichero 
            File f = new File(FICHERO);

            // Si el fichero no existe, devuelve el mapa vacío
            if (!f.exists()) {
                return mapa;
            }

            // Abre el fichero para lectura
            BufferedReader br = new BufferedReader(new FileReader(f));

            // obtiene la línea del fichero
            String cifrado = br.readLine();

            // Cierra el fichero
            br.close();

            // Si el fichero está vacío, devuelve mapa vacío
            if (cifrado == null) {
                return mapa;
            }

            // Descifra el contenido con AES
            String contenido = CifradoAES.descifrar(cifrado);

            // Divide el contenido en líneas/usuarios
            for (String linea : contenido.split("\n")) {

                // Divide cada línea en email y valor usando ":"
                String[] partes = linea.split(":");

                // Comprueba que la línea tenga el formato correcto
                if (partes.length == 2) {

                    // Se añade al mapa 
                    mapa.put(partes[0], partes[1]);
                }
            }

            // Devuelve el mapa con todos los usuarios
            return mapa;

        } finally {
            
            // Libera el bloqueo de lectura
            lock.readLock().unlock();
        }
    }

    public static void guardarUsuarios(Map<String, String> mapa) throws Exception {
        // Bloquear el acceso en modo escritura y nadie puede leer mientras escribe
        lock.writeLock().lock();

        try {
            // StringBuilder para construir el contenido del fichero en memoria
            StringBuilder sb = new StringBuilder();

            // Recorre todos los usuarios del mapa
            for (String email : mapa.keySet()) {

                sb.append(email)
                        .append(":")
                        .append(mapa.get(email))
                        .append("\n");
            }

            // Cifra todo el contenido usando AES 
            String cifrado = CifradoAES.cifrar(sb.toString());

            // Se obtiene el fichero
            PrintWriter pw = new PrintWriter(new FileWriter(FICHERO));

            // Escribe el contenido cifrado
            pw.print(cifrado);

            pw.close();

        } finally {
            // Libera el bloqueo 
            lock.writeLock().unlock();
        }
    }
}
