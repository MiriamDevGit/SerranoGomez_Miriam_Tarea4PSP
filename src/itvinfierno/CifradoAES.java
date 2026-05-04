package itvinfierno;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 *
 * @author Miriam
 */

public class CifradoAES {

    

    public static String cifrar(String texto) throws Exception {
        SecretKeySpec clave = new SecretKeySpec("1234567890123456".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        
        return Base64.getEncoder().encodeToString(cipher.doFinal(texto.getBytes()));
    }

    public static String descifrar(String texto) throws Exception {
        SecretKeySpec clave = new SecretKeySpec("1234567890123456".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        
        cipher.init(Cipher.DECRYPT_MODE, clave);
        
        return new String(cipher.doFinal(Base64.getDecoder().decode(texto)));
    }
}
