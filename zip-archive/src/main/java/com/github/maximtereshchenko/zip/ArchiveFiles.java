package com.github.maximtereshchenko.zip;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ArchiveFiles extends SimpleFileVisitor<Path> {

    private final Path base;
    private final ZipOutputStream zipOutputStream;

    ArchiveFiles(Path base, ZipOutputStream zipOutputStream) {
        this.base = base;
        this.zipOutputStream = zipOutputStream;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(base.relativize(file).toString()));
        zipOutputStream.write(Files.readAllBytes(file));
        return FileVisitResult.CONTINUE;
    }
}
