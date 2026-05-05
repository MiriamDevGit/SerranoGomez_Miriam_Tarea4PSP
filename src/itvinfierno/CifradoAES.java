package itvinfierno;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 *
 * @author Miriam
 */
public class CifradoAES {

    public static byte[] cifrar(String texto) throws Exception {
        SecretKeySpec clave = new SecretKeySpec("1234567890123456".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, clave);

        return cipher.doFinal(texto.getBytes());
    }

    public static String descifrar(byte[] datos) throws Exception {
        SecretKeySpec clave = new SecretKeySpec("1234567890123456".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, clave);

        return new String(cipher.doFinal(datos));
    }
}
