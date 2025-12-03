import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

public class Crypto {

    // Generate random salt (16 bytes)
    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(salt);
        return salt;
    }

    // Generate random IV (16 bytes)
    public static byte[] generateIV() throws NoSuchAlgorithmException {
        byte[] iv = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        return iv;
    }

    // Derive AES 256-bit key using password + salt
    public static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        int iterations = 30000;
        int keyLength = 256;
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKey key = factory.generateSecret(spec);
        return new SecretKeySpec(key.getEncoded(), "AES");
    }

    // Create AES/CBC cipher
    public static Cipher createCipher(int mode, SecretKeySpec key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, key, new IvParameterSpec(iv));
        return cipher;
    }

    // MD5 hash for integrity check
    public static String md5Hex(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream in = Files.newInputStream(file.toPath())) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                md.update(buf, 0, n);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    // Copy exact number of bytes
    public static void copyExact(InputStream in, OutputStream out, long bytes) throws IOException {
        byte[] buf = new byte[8192];
        long remaining = bytes;
        while (remaining > 0) {
            int read = in.read(buf, 0, (int) Math.min(buf.length, remaining));
            if (read == -1)
                throw new EOFException("Unexpected end of stream");
            out.write(buf, 0, read);
            remaining -= read;
        }
    }

    // Copy entire stream
    public static void copyAll(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }
}
