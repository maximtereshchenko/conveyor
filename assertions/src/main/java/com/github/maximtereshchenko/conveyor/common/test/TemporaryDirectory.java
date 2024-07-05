package com.github.maximtereshchenko.conveyor.common.test;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

final class TemporaryDirectory implements ExtensionContext.Store.CloseableResource {

    private final Path path;

    TemporaryDirectory(Path path) {
        this.path = path;
    }

    @Override
    public void close() throws Throwable {
        if (!Files.exists(path)) {
            return;
        }
        Files.walkFileTree(
            path,
            new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            }
        );
    }
}
