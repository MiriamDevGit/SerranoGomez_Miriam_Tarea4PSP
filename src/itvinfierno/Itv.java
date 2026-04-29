package itvinfierno;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Miriam Serrano Gómez
 *
 */
public class Itv {

    private final int MAX_LINEAS = 4;
    private int cochesDentro = 0;

    private static final ConcurrentHashMap<String, Integer> citas = new ConcurrentHashMap<>();

    public synchronized int intentarEntrar(String matricula) throws InterruptedException {

        while (cochesDentro >= MAX_LINEAS) {
            System.out.println(matricula + " esperando para entrar.");
            wait();
        }
        cochesDentro++;

        for (int i = 1; i <= MAX_LINEAS; i++) {

            boolean ocupada = false;
            //comprueba si las lineas están ocupadas 
            for (Integer linea : citas.values()) {
                if (linea == i) {
                    ocupada = true;
                    break;
                }
            }
            // devuelve linea libre
            if (!ocupada) {
                return i;
            }
        }

        return -1;
    }

    public synchronized void salir() {
        cochesDentro--;
        notifyAll();
    }

    public String generarPanel() {
        StringBuilder sb = new StringBuilder();

        // --- Citas (texto simple arriba del panel)
        sb.append("<div style='color:white; font-size:18px;'>");
        sb.append("<strong>CITAS</strong><br>");
        sb.append("-----------------------------<br>");

        for (Map.Entry<String, Integer> entry : citas.entrySet()) {
            if (entry.getValue() == 0) {   // solo citas pendientes
                sb.append(entry.getKey())
                        .append("<br>");
            }
        }
        sb.append("<br><strong>LINEAS DE INSPECCIÓN</strong><br>")
                .append("-----------------------------<br><br>")
                .append("</div>");

        // --- Panel principal tipo LED
        for (int i = 1; i <= MAX_LINEAS; i++) {
            String matricula = "LIBRE";
            boolean libre = true;
            boolean encontrada = false;

            for (Map.Entry<String, Integer> entry : citas.entrySet()) {

                if (!encontrada && entry.getValue() == i) {
                    matricula = entry.getKey();
                    libre = false;
                    encontrada = true;
                }
            }

            String color = libre ? "verde" : "rojo";

            sb.append(
                    "<div class=\"linea\">"
                    + "<div class=\"" + color + "\">" + matricula + "</div>"
                    + "<div class=\"" + color + "\">" + i + "</div>"
                    + "</div>"
            );
        }

        return sb.toString();
    }

    public void reservar(String matricula) {
        citas.put(matricula, 0);
    }

    public static boolean existeCita(String matricula) {
        return citas.containsKey(matricula) && citas.get(matricula) == 0;
    }

    public static void asignarLinea(String matricula, int linea) {
        System.out.println(matricula + " entra en linea " + linea);
        citas.put(matricula, linea);
    }

    public static void liberarLinea(String matricula) {
        citas.remove(matricula);
    }
}
