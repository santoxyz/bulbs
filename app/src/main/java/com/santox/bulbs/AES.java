package com.santox.bulbs;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Created by s on 15/04/2020.
 */

public class AES {
    public static byte key[] = {(byte)0x09, (byte)0x76, (byte)0x28, (byte)0x34, (byte)0x3f, (byte)0xe9, (byte)0x9e, (byte)0x23,
            (byte)0x76, (byte)0x5c, (byte)0x15, (byte)0x13, (byte)0xac, (byte)0xcf, (byte)0x8b, (byte)0x02};
    public static byte iv[] = {(byte)0x56, (byte)0x2e, (byte)0x17, (byte)0x99, (byte)0x6d, (byte)0x09, (byte)0x3d, (byte)0x28,
            (byte)0xdd, (byte)0xb3, (byte)0xba, (byte)0x69, (byte)0x5a, (byte)0x2e, (byte)0x6f, (byte)0x58};


    public static byte[] encrypt(byte[] clean) throws Exception {
        //byte[] clean = plainText.getBytes();

        // Generating IV.
        //int ivSize = 16;
        //byte[] iv = new byte[ivSize];
        //SecureRandom random = new SecureRandom();
        //random.nextBytes(iv);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);


        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key);
        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);

        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        // Combine IV and encrypted part.
        byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);

        return encryptedIVAndText;
    }

    public static byte[] decrypt(byte[] encryptedIvTextBytes) throws Exception {

        // Extract IV.
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Extract encrypted part.
        int encryptedSize = encryptedIvTextBytes.length - iv.length;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, iv.length, encryptedBytes, 0, encryptedSize);

        // Hash key.
        byte[] keyBytes = new byte[key.length];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key);
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return decrypted;
    }
}
