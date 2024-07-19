package com.github.maximtereshchenko.conveyor.springboot;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

final class Copy extends SimpleFileVisitor<Path> {

    private final Path source;
    private final Path destination;

    Copy(Path source, Path destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        var fileDestination = resolved(file);
        Files.createDirectories(fileDestination.getParent());
        Files.copy(file, fileDestination);
        return FileVisitResult.CONTINUE;
    }

    private Path resolved(Path original) {
        var result = destination;
        for (var part : source.relativize(original)) {
            result = result.resolve(part.toString());
        }
        return result;
    }
}
