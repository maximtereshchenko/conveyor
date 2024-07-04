package com.github.maximtereshchenko.conveyor.files;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public final class Copy extends SimpleFileVisitor<Path> {

    private final Path source;
    private final Path destination;

    public Copy(Path source, Path destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        var resolved = destination.resolve(source.relativize(file));
        Files.createDirectories(resolved.getParent());
        Files.copy(file, resolved, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }
}
