package com.github.maximtereshchenko.conveyor.zip;

import com.github.maximtereshchenko.conveyor.filevisitors.Generic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipArchiveContainer {

    private final Path path;

    public ZipArchiveContainer(Path path) {
        this.path = path;
    }

    public void archive(Path target) {
        try (var zipOutputStream = new ZipOutputStream(Files.newOutputStream(target))) {
            Files.walkFileTree(path, new Generic(file -> write(file, path, zipOutputStream)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(Path file, Path base, ZipOutputStream outputStream) throws IOException {
        outputStream.putNextEntry(new ZipEntry(base.relativize(file).toString()));
        outputStream.write(Files.readAllBytes(file));
    }
}
