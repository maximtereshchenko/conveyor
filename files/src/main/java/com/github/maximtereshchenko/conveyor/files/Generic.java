package com.github.maximtereshchenko.conveyor.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class Generic extends SimpleFileVisitor<Path> {

    private final IOConsumer<Path> consumer;

    public Generic(IOConsumer<Path> consumer) {
        this.consumer = consumer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        consumer.accept(file);
        return FileVisitResult.CONTINUE;
    }
}
