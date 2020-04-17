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
    public static byte key[] = {(byte)(0x09 & 0xff), (byte)(0x76 & 0xff), (byte)(0x28 & 0xff), (byte)(0x34 & 0xff), (byte)(0x3f & 0xff), (byte)(0xe9 & 0xff), (byte)(0x9e & 0xff), (byte)(0x23 & 0xff),
            (byte)(0x76 & 0xff), (byte)(0x5c & 0xff), (byte)(0x15 & 0xff), (byte)(0x13 & 0xff), (byte)(0xac & 0xff), (byte)(0xcf & 0xff), (byte)(0x8b & 0xff), (byte)(0x02 & 0xff)};
    public static byte iv[] = {(byte)(0x56 & 0xff), (byte)(0x2e & 0xff), (byte)(0x17 & 0xff), (byte)(0x99 & 0xff), (byte)(0x6d & 0xff), (byte)(0x09 & 0xff), (byte)(0x3d & 0xff), (byte)(0x28 & 0xff),
            (byte)(0xdd & 0xff), (byte)(0xb3 & 0xff), (byte)(0xba & 0xff), (byte)(0x69 & 0xff), (byte)(0x5a & 0xff), (byte)(0x2e & 0xff), (byte)(0x6f & 0xff), (byte)(0x58 & 0xff)};


    public static byte[] encrypt(byte[] clean) throws Exception {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(clean);
    }

    public static byte[] decrypt(byte[] encryptedBytes /*encryptedIvTextBytes*/) throws Exception {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/NoPadding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipherDecrypt.doFinal(encryptedBytes);
    }
}
