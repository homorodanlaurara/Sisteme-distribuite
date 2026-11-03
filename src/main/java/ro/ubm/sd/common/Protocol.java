package ro.ubm.sd.common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Protocol {

    public static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String checksum(Path file) throws IOException {
        MessageDigest sha = sha256();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                sha.update(buffer, 0, n);
            }
        }
        return HexFormat.of().formatHex(sha.digest());
    }

    public static void writeHeader(DataOutputStream out, String header) throws IOException {
        out.writeUTF(header);
        out.flush();
    }

    public static String[] readHeader(DataInputStream in) throws IOException {
        String header = in.readUTF();
        return header.split(" ");
    }

    public static String receiveBytes(InputStream in, long size, Path target) throws IOException {
        MessageDigest sha = sha256();
        byte[] buffer = new byte[64 * 1024];
        long remaining = size;
        try (OutputStream out = Files.newOutputStream(target)) {
            while (remaining > 0) {
                int n = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (n < 0) {
                    throw new EOFException("conexiune inchisa dupa " + (size - remaining) + " din " + size + " octeti");
                }
                out.write(buffer, 0, n);
                sha.update(buffer, 0, n);
                remaining -= n;
            }
        }
        return HexFormat.of().formatHex(sha.digest());
    }
}