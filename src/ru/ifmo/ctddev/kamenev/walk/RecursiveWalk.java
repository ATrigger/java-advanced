package ru.ifmo.ctddev.kamenev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by kamenev on 10.02.16.
 */
public class RecursiveWalk extends Walk {
    public static void main(String[] args) {
        if (args.length < 2)
            throw new IllegalArgumentException("None input\\output specified");
        Walk walker = new RecursiveWalk();
        try {
            walker.initialize(args);
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        walker.process();
    }

    @Override
    public void process() {
        try {
            try (BufferedReader input = Files.newBufferedReader(files[0], StandardCharsets.UTF_8);
                 BufferedWriter output = Files.newBufferedWriter(files[1], StandardCharsets.UTF_8)) {
                for (String line; (line = input.readLine()) != null; ) {
                    Path current = Paths.get(line);
                    if (!isDir(current)) {
                        output.write(Utils.calculateMD5(current));
                        output.write(' ');
                        output.write(line);
                        output.newLine();
                    } else {
                        DirWalker dw = new DirWalker(output);
                        Files.walkFileTree(current, dw);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private Boolean isDir(Path path) {
        return !(path == null || !Files.exists(path)) && Files.isDirectory(path);
    }
}
