package itvinfierno;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Miriam Serrano Gómez
 */
public class HiloServidor implements Runnable {

    private final int OK = 200;
    private final int NOTFOUND = 404;
    private Socket socket;
    private Itv itv;
    private Random random = new Random();

    private final String[] pruebas = {
        "Luces", "Frenos", "Emisiones", "Dirección", "Suspensión"
    };

    private final String[] frasesProhibidas = {
        "ok jefe", "lo que tú digas", "a mandar", "como usted mande",
        "vamos al lío", "marchando", "manda usted", "perfecto máquina",
        "de lujo"
    };
    private static final String[] frasesPermitidas = {
        "vale", "recibido", "entendido", "procedo", "hecho",
        "si", "correcto", "ok", "de acuerdo"
    };

    private boolean emailValido(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean passwordValida(String pass) {
        return pass.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
    }

    public HiloServidor(Socket socket, Itv itv) {
        this.socket = socket;
        this.itv = itv;

    }

    @Override
    public void run() {
        try (Socket s = this.socket;
                BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter salida = new PrintWriter(s.getOutputStream(), true);) {
            //Recoge la petición
            String peticion = entrada.readLine();
            // Comprueba que la petición no sea nula
            if (peticion != null && (peticion.startsWith("GET") || peticion.startsWith("POST"))) {

                //String ruta = peticion.split(" ")[1];
                // extrae la ruta de la petición
                String ruta = peticion.split(" ")[1].split("\\?")[0];
                int contentLength = 0;
                String linea;

                // extrae el tamaño del cuerpo
                while (!(linea = entrada.readLine()).isEmpty()) {
                    if (linea.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(linea.split(":")[1].trim());
                    }
                }
                // extrae el cuerpo 
                StringBuilder cuerpo = new StringBuilder();
                if (contentLength > 0) {
                    for (int i = 0; i < contentLength; i++) {
                        cuerpo.append((char) entrada.read());
                    }
                }
                // guarda el string cuerpo en body
                String body = cuerpo.toString();
                String respuestaHTML;

                // imprime la ruta
                //System.out.println("Ruta: " + ruta);
                if (ruta.equals("/") && peticion.startsWith("GET")) {
                    // abre login
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.login(""));
                } else if (ruta.equals("/registro") && peticion.startsWith("POST")) {
                    // registrar usuario
                    respuestaHTML = construirRespuesta(OK, registrarUsuario(body));

                } else if (ruta.equals("/inicio") && peticion.startsWith("POST")) {
                    // loguear usuario
                    respuestaHTML = construirRespuesta(OK, loguear(body));

                } else if (ruta.equals("/inicio") && peticion.startsWith("GET")) {
                    // abre index
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(itv.generarPanel()));

                } else if (ruta.equals("/reservar") && peticion.startsWith("GET")) {
                    //abre reservar
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlReservar);
                    // para las pruebas, introduce 6 matrículas
                    reservarVariasMatriculas();

                } else if (ruta.equals("/reservar") && peticion.startsWith("POST")) {
                    // obtiene la matrícula 
                    String matricula = extraerMatricula(body);
                    //reserva la matricula
                    itv.reservar(matricula);
                    // actualiza el panel
                    String panel = itv.generarPanel();
                    // redirige a index
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlIndex(panel));

                } else if (ruta.equals("/pasar") && peticion.startsWith("GET")) {
                    // aber pasar
                    respuestaHTML = construirRespuesta(OK, PaginasHTML.htmlPasarITV("", ""));

                } else if (ruta.equals("/pasar") && peticion.startsWith("POST")) {
                    // obtiene la matrícula
                    String matricula = extraerMatricula(body);
                    // simula la itv de la matrícula
                    String resultado = simularITV(matricula);
                    // abre /pasar con parámetros
                    respuestaHTML = construirRespuesta(OK,
                            PaginasHTML.htmlPasarITV(matricula, resultado));

                } else {
                    //abre not found
                    respuestaHTML = construirRespuesta(NOTFOUND, PaginasHTML.html_notFound);
                }
                // envia la respuesta html al navegador
                salida.print(respuestaHTML);
                salida.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// función para pruebas, la dejo para facilitar la corrección
    private void reservarVariasMatriculas() {
        itv.reservar("1111AAA");
        itv.reservar("2222BBB");
        itv.reservar("3333CCC");
        itv.reservar("4444DDD");
        itv.reservar("5555EEE");
        itv.reservar("6666FFF");

    }

    private String registrarUsuario(String body) {
        try {
            // Se extraen los parámetros del body
            String email = extraerParametro(body, "email");
            String password = extraerParametro(body, "password");

            if (!emailValido(email)) {
                return PaginasHTML.login("Email inválido");
            }

            if (!passwordValida(password)) {
                Log.escribir("La contraseña no es válida: " + password);
                return PaginasHTML.login("Contraseña inválida");
            }

            Map<String, String> usuarios = Usuario.leerUsuarios();

            if (usuarios.containsKey(email)) {
                return PaginasHTML.login("El usuario ya existe");
            }
            // Se hace el registro
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            usuarios.put(email, hash);

            Usuario.guardarUsuarios(usuarios);

            return PaginasHTML.login("Registrado correctamente");

        } catch (Exception e) {
            return PaginasHTML.login("Error en el registro");
        }
    }

    private String loguear(String body) {
        try {
            // Se extraen los parámetros del body
            String email = extraerParametro(body, "email");
            String password = extraerParametro(body, "password");

            Map<String, String> usuarios = Usuario.leerUsuarios();

            String hash = usuarios.get(email);

            // Si es correcto, se abre la página de inicio
            if (hash != null && BCrypt.checkpw(password, hash)) {
                return PaginasHTML.htmlIndex(itv.generarPanel());
            } else {
                Log.escribir("Login incorrecto: " + email);
                return PaginasHTML.login("Credenciales incorrectas");
            }

        } catch (Exception e) {
            return PaginasHTML.login("Error en el login");
        }
    }

    private boolean nuevaProbabilidad(int prob) {
        return Math.random() * 100 < prob;
    }

    public String construirRespuesta(int codigo, String contenido) {
        return (codigo == 200 ? "HTTP/1.1 200 OK" : "HTTP/1.1 404 Not Found") + "\n"
                + "Content-Type: text/html; charset=UTF-8" + "\n"
                + "Content-Length: " + contenido.length() + "\n"
                + "\n"
                + contenido;

    }

    private String extraerMatricula(String body) {
        try {
            return body.split("=")[1].toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }

    private String simularITV(String matricula) {
        try {
            if (!Itv.existeCita(matricula)) {
                return "<p>No tienes cita previa</p>";
            }

            int linea = itv.intentarEntrar(matricula);
            // Guarda matricula en citas
            Itv.asignarLinea(matricula, linea);

            int probabilidad = 60;
            boolean aprobado = true;
            StringBuilder resultado = new StringBuilder();

            for (String prueba : pruebas) {

                Thread.sleep(1000 + random.nextInt(10000));

                String respuesta = fraseAleatoria();

                for (String frase : frasesProhibidas) {
                    if (frase.equalsIgnoreCase(respuesta)) {
                        probabilidad -= 10;
                    }
                }

                boolean ok = nuevaProbabilidad(probabilidad);
                if (!ok) {
                    aprobado = false;
                }

                resultado.append("<p>")
                        .append(prueba)
                        .append(": ")
                        .append(ok ? "OK" : "NO OK")
                        .append(" (").append(respuesta).append(" - prob ")
                        .append(probabilidad).append("%)")
                        .append("</p>");
            }

            itv.salir();
            Itv.liberarLinea(matricula);

            if (aprobado) {
                return PaginasHTML.htmlResultado("<b style='color:lightgreen;'>ITV SUPERADA</b>" + resultado);
            } else {
                return PaginasHTML.htmlResultado("<b style='color:red;'>ITV NO SUPERADA</b>" + resultado);
            }

        } catch (Exception e) {
            return "<p>Error en la inspección</p>";
        }
    }

    private String fraseAleatoria() {
        if (Math.random() < 0.7) {
            return frasesPermitidas[random.nextInt(frasesPermitidas.length)];
        } else {
            return frasesProhibidas[random.nextInt(frasesProhibidas.length)];
        }
    }

    private String extraerParametro(String body, String nombre) {
        try {
            // Se divide el body en pares clave=valor separados por "&"
            String[] pares = body.split("&");

            // Recorre cada pareja clave=valor
            for (String par : pares) {
                // Divide cada clave y valor usando "="
                String[] kv = par.split("=");

                // Si la clave coincide con el nombre, devuelve el valor
                if (kv[0].equals(nombre)) {

                    // Devuelve el valor decodificado por si el correo viene con %40
                    return java.net.URLDecoder.decode(kv[1], "UTF-8");
                }
            }

        } catch (Exception e) {
        }
        // Devuelve vacío si no encuentra el parámetro
        return "";
    }

}
