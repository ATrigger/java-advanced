package ru.ifmo.ctddev.kamenev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class RecursiveWalk {
    protected Path[] files;

    public static void main(String[] args) {
        if (args.length == 2) {
            RecursiveWalk walker = new RecursiveWalk();
            walker.files = new Path[2];
            walker.files[0] = Paths.get(args[0]);
            if (Files.notExists(walker.files[0])) {
                System.out.println("Input file doesn't exist");
                return;
            }
            walker.files[1] = Paths.get(args[1]);
            walker.process();
        } else System.err.println("No input\\output file specified");
    }

    public void process() {
        try {
            try (BufferedReader input = Files.newBufferedReader(files[0], StandardCharsets.UTF_8);
                 BufferedWriter output = Files.newBufferedWriter(files[1], StandardCharsets.UTF_8)) {
                for (String line; (line = input.readLine()) != null; ) {
                    Path current = Paths.get(line);
                    DirWalker dw = new DirWalker(output);
                    Files.walkFileTree(current, dw);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public class Hasher {
        public static final String MD5_ZERO = "00000000000000000000000000000000";

        public String calculateMD5(Path filePath) {
            try {
                try (InputStream file = Files.newInputStream(filePath)) {
                    byte[] dataBytes = new byte[1024];
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    for (int n; (n = file.read(dataBytes)) != -1; )
                        md.update(dataBytes, 0, n);
                    byte[] mdbytes = md.digest();
                    StringBuilder sb = new StringBuilder();
                    for (byte mdbyte : mdbytes) sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
                    return sb.toString().toUpperCase();
                }
            } catch (Exception e) { // Couldn't calculate hash
                return MD5_ZERO;
            }
        }
    }

    public class DirWalker
            extends SimpleFileVisitor<Path> {
        private final BufferedWriter output;
        private Hasher hasher;

        DirWalker(BufferedWriter output) {
            hasher = new Hasher();
            this.output = output;
        }

        private FileVisitResult writeResult(String result, Path file) {
            try {
                output.write(result + ' ' + file.toString());
                output.newLine();
            } catch (IOException e) {
                return TERMINATE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attr) {
            return writeResult(hasher.calculateMD5(file), file);
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                                               IOException exc) {
            return writeResult(Hasher.MD5_ZERO, file);
        }
    }
}

