package ru.ifmo.ctddev.kamenev.walk;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * Created by kamenev on 10.02.16.
 */
public class Utils {
    public static String calculateMD5(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream file = Files.newInputStream(filePath)) {
                byte[] dataBytes = new byte[1024];
                int n;
                while ((n = file.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, n);
                }
                byte[] mdbytes = md.digest();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                return sb.toString().toUpperCase();
            }
        } catch (Exception e) { // Couldn't calculate hash
            return "00000000000000000000000000000000";
        }
    }
}
