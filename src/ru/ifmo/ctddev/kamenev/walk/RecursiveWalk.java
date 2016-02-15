package ru.ifmo.ctddev.kamenev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
 * Created by kamenev on 10.02.16.
 */
public class RecursiveWalk {
    protected Path[] files;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("No input file specified");
            return;
        }
        RecursiveWalk walker = new RecursiveWalk();
        try {
            walker.initialize(args);
        } catch (FileNotFoundException e) {
            System.out.println(e);
            return;
        }
        walker.process();
    }

    public void initialize(String[] input) throws FileNotFoundException {
        files = new Path[2];
        files[0] = Paths.get(input[0]);

        files[1] = Paths.get(input[1]);
        if (!files[0].toFile().exists()) {
            throw new FileNotFoundException("Input file not found");
        }
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

    public class DirWalker
            extends SimpleFileVisitor<Path> {
        private final BufferedWriter output;

        DirWalker(BufferedWriter output) {
            this.output = output;
        }
        private FileVisitResult writeResult(String result, Path file){
            try {
                output.write(result);
                output.write(' ');
                output.write(file.toString());
                output.newLine();
            } catch (IOException e) {
                System.err.println(e);
                return TERMINATE;
            }
            return CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attr) {
            return writeResult(Utils.calculateMD5(file),file);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir,
                                                  IOException exc) {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                                               IOException exc) {
            return writeResult("00000000000000000000000000000000",file);
        }
    }
}
