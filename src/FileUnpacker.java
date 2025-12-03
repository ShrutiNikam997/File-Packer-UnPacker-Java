import javax.crypto.CipherInputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.*;

public class FileUnpacker {

    private static final String MAGIC = "FPACK2025";

    public static void unpackFiles(String packedFile, String outputFolder, String password) throws Exception {

        File pack = new File(packedFile);
        if (!pack.exists()) {
            System.out.println("Pack file not found.");
            return;
        }

        File dir = new File(outputFolder);
        if (!dir.exists())
            dir.mkdirs();

        try (
                FileInputStream fis = new FileInputStream(pack);
                DataInputStream dis = new DataInputStream(new BufferedInputStream(fis))) {

            String magic = dis.readUTF();
            if (!MAGIC.equals(magic)) {
                System.out.println("Invalid pack file.");
                return;
            }

            int version = dis.readInt();
            if (version != 1) {
                System.out.println("Unsupported version.");
                return;
            }

            int saltLen = dis.readInt();
            byte[] salt = new byte[saltLen];
            dis.readFully(salt);

            int ivLen = dis.readInt();
            byte[] iv = new byte[ivLen];
            dis.readFully(iv);

            int count = dis.readInt();

            List<Meta> metas = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                metas.add(new Meta(dis.readUTF(), dis.readLong(), dis.readUTF()));
            }

            SecretKeySpec key = Crypto.deriveKey(password, salt);
            Cipher cipher = Crypto.createCipher(Cipher.DECRYPT_MODE, key, iv);

            CipherInputStream cis = new CipherInputStream(dis, cipher);

            for (Meta m : metas) {
                System.out.println("Extracting: " + m.name);
                File out = new File(dir, m.name);
                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(out))) {
                    Crypto.copyExact(cis, fos, m.size);
                }

                // MD5 check
                String md5 = Crypto.md5Hex(out);
                if (!md5.equals(m.md5)) {
                    System.out.println("WARNING: MD5 mismatch in " + m.name);
                } else {
                    System.out.println("Verified: " + m.name);
                }
            }
        }

        System.out.println("Unpacking complete → " + outputFolder);
    }

    private static class Meta {
        String name, md5;
        long size;

        Meta(String n, long s, String m) {
            name = n;
            size = s;
            md5 = m;
        }
    }
}
