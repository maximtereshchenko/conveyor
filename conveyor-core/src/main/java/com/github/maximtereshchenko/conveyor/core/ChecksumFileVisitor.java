package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

final class ChecksumFileVisitor extends SimpleFileVisitor<Path> {

    private final Checksum checksum = new CRC32C();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        checksum.update(file.toString().getBytes(StandardCharsets.UTF_8));
        checksum.update(Files.readAllBytes(file));
        return FileVisitResult.CONTINUE;
    }

    long checksum() {
        return checksum.getValue();
    }
}
