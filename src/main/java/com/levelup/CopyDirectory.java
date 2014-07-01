package com.levelup;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.FileVisitResult.*;

public class CopyDirectory implements FileVisitor<Path> {
    private final Path source;
    private final Path target;

    public CopyDirectory(Path source, Path target){
        this.source = source;
        this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        Path newDir = target.resolve(source.relativize(dir));
        try {
            Files.copy(dir, newDir, REPLACE_EXISTING);
        } catch (IOException x) {
            System.err.format("Unable to create: %s: %s%n", newDir, x);
            return SKIP_SUBTREE;
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        try {
            Files.copy(file, target.resolve(source.relativize(file)), REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.format("Unable to copy: %s: %s%n", file, e);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return CONTINUE;
    }
}
