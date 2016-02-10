package ru.ifmo.ctddev.kamenev.walk;
/**
 * Created by kamenev on 10.02.16.
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class DirWalker
        extends SimpleFileVisitor<Path> {
    private final BufferedWriter output;

    DirWalker(BufferedWriter output) {
        this.output = output;
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr) {
        try {
            output.write(Utils.calculateMD5(file));
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
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exc) {
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
        return CONTINUE;
    }
}
