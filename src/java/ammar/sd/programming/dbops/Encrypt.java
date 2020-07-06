/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ammar.sd.programming.dbops;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Ammar Abbas
 */
public class Encrypt {
    
    public String Encrypt(String message) {
        try {
            final MessageDigest md = MessageDigest.getInstance("md5");//ABCDEF
            final byte[] digestOfPassword = md.digest("ABCDEF@2020AS".getBytes("UTF-8"));
            final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8; ) {
                keyBytes[k++] = keyBytes[j++];
            }
            KeySpec keySpec = new DESedeKeySpec(keyBytes);
            SecretKey key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
            IvParameterSpec iv = new IvParameterSpec((new byte[8]));
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] plainTextBytes = message.getBytes("UTF-8");
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            return new String(Base64.encodeBase64(cipherText), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String Decrypt(String message) {
        try {
            final MessageDigest md = MessageDigest.getInstance("md5");
            final byte[] digestOfPassword = md.digest("FEDCBA@AS2020".getBytes("UTF-8"));
            final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8; ) {
                keyBytes[k++] = keyBytes[j++];
            }
            KeySpec keySpec = new DESedeKeySpec(keyBytes);
            SecretKey key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
            final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
            Cipher dcipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            dcipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] dec = Base64.decodeBase64(message.getBytes());
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 
}
