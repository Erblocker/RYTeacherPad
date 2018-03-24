package io.vov.vitamio.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.protocol.HTTP;

public class Crypto {
    private Cipher ecipher;

    public Crypto(String key) {
        try {
            setupCrypto(new SecretKeySpec(generateKey(key), "AES"));
        } catch (Throwable e) {
            Log.e("Crypto", e);
        }
    }

    private void setupCrypto(SecretKey key) {
        byte[] iv = new byte[16];
        iv[1] = (byte) 1;
        iv[2] = (byte) 2;
        iv[3] = (byte) 3;
        iv[4] = (byte) 4;
        iv[5] = (byte) 5;
        iv[6] = (byte) 6;
        iv[7] = (byte) 7;
        iv[8] = (byte) 8;
        iv[9] = (byte) 9;
        iv[10] = (byte) 10;
        iv[11] = (byte) 11;
        iv[12] = (byte) 12;
        iv[13] = (byte) 13;
        iv[14] = (byte) 14;
        iv[15] = (byte) 15;
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        try {
            this.ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            this.ecipher.init(1, key, paramSpec);
        } catch (Throwable e) {
            this.ecipher = null;
            Log.e("setupCrypto", e);
        }
    }

    public String encrypt(String plaintext) {
        if (this.ecipher == null) {
            return "";
        }
        try {
            return Base64.encodeToString(this.ecipher.doFinal(plaintext.getBytes(HTTP.UTF_8)), 2);
        } catch (Throwable e) {
            Log.e("encryp", e);
            return "";
        }
    }

    public static String md5(String plain) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(plain.getBytes());
            String hashtext = new BigInteger(1, m.digest()).toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] generateKey(String input) {
        try {
            return MessageDigest.getInstance("SHA256").digest(input.getBytes(HTTP.UTF_8));
        } catch (Throwable e) {
            Log.e("generateKey", e);
            return null;
        }
    }

    private PublicKey readKeyFromStream(InputStream keyStream) throws IOException {
        ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(keyStream));
        try {
            PublicKey pubKey = (PublicKey) oin.readObject();
            return pubKey;
        } catch (Throwable e) {
            Log.e("readKeyFromStream", e);
            return null;
        } finally {
            oin.close();
        }
    }

    public String rsaEncrypt(InputStream keyStream, String data) {
        try {
            return rsaEncrypt(keyStream, data.getBytes(HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public String rsaEncrypt(InputStream keyStream, byte[] data) {
        try {
            PublicKey pubKey = readKeyFromStream(keyStream);
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(1, pubKey);
            return Base64.encodeToString(cipher.doFinal(data), 2);
        } catch (Throwable e) {
            Log.e("rsaEncrypt", e);
            return "";
        }
    }
}
