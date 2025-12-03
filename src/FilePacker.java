import javax.crypto.CipherOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.*;

public class FilePacker {

    private static final String MAGIC = "FPACK2025";
    private static final int VERSION = 1;

    public static void packFiles(String outputFile, String password, List<File> files) throws Exception {

        List<Meta> metas = new ArrayList<>();

        for (File f : files) {
            if (!f.exists() || !f.isFile()) {
                System.out.println("Skipping (not found): " + f.getAbsolutePath());
                continue;
            }
            metas.add(new Meta(f.getName(), f.length(), Crypto.md5Hex(f), f));
        }

        if (metas.isEmpty()) {
            System.out.println("No valid files to pack.");
            return;
        }

        byte[] salt = Crypto.generateSalt();
        byte[] iv = Crypto.generateIV();
        SecretKeySpec key = Crypto.deriveKey(password, salt);

        try (
                FileOutputStream fos = new FileOutputStream(outputFile);
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos))) {
            dos.writeUTF(MAGIC);
            dos.writeInt(VERSION);

            dos.writeInt(salt.length);
            dos.write(salt);

            dos.writeInt(iv.length);
            dos.write(iv);

            dos.writeInt(metas.size());

            for (Meta m : metas) {
                dos.writeUTF(m.name);
                dos.writeLong(m.size);
                dos.writeUTF(m.md5);
            }
            dos.flush();

            Cipher cipher = Crypto.createCipher(Cipher.ENCRYPT_MODE, key, iv);

            try (CipherOutputStream cos = new CipherOutputStream(dos, cipher)) {
                for (Meta m : metas) {
                    System.out.println("Packing: " + m.name);
                    try (InputStream fis = new FileInputStream(m.fileObj)) {
                        Crypto.copyAll(fis, cos);
                    }
                }
            }
        }

        System.out.println("Packing complete → " + outputFile);
    }

    private static class Meta {
        String name, md5;
        long size;
        File fileObj;

        Meta(String n, long s, String m, File f) {
            name = n;
            size = s;
            md5 = m;
            fileObj = f;
        }
    }
}
