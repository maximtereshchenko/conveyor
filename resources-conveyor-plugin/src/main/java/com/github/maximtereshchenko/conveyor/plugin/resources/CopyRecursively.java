package com.github.maximtereshchenko.conveyor.plugin.resources;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

final class CopyRecursively extends SimpleFileVisitor<Path> {

    private final Path source;
    private final Path target;

    CopyRecursively(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException {
        Files.createDirectories(resolved(dir));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, resolved(file));
        return FileVisitResult.CONTINUE;
    }

    private Path resolved(Path path) {
        return target.resolve(source.relativize(path));
    }
}
