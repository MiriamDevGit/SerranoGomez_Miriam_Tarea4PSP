package itvinfierno;

import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author Miriam
 */
public class CifradoBCrypt {
    public static void main(String[] args){
        String palabra = "hola1234";
        
        String hash = BCrypt.hashpw(palabra, BCrypt.gensalt(12));
        
        boolean coincide = BCrypt.checkpw("hola1234", hash);
        
        System.out.println("coincide: " + coincide);
    }
}
