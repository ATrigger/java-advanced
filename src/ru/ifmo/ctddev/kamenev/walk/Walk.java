/**
 * Created by kamenev on 10.02.16.
 */
//CLASSPATH=${CLASSPATH}:$(echo "$(pwd)"/*.jar | tr ' ' ':')
//java info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.kamanev.walk.RecursiveWalk
package ru.ifmo.ctddev.kamenev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {
    protected Path[] files;

    public static void main(String[] args) {
        if (args.length < 2)
            throw new IllegalArgumentException("None input\\output specified");
        Walk walker = new Walk();
        try {
            walker.initialize(args);
        } catch (FileNotFoundException e) {
            System.out.println(e);
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
                    output.write(Utils.calculateMD5(current));
                    output.write(' ');
                    output.write(line);
                    output.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
