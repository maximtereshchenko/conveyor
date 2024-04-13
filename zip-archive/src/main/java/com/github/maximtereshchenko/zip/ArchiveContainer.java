package com.github.maximtereshchenko.zip;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

public final class ArchiveContainer {

    private final Path path;

    public ArchiveContainer(Path path) {
        this.path = path;
    }

    public void archive(Path target) {
        try (var zipOutputStream = new ZipOutputStream(Files.newOutputStream(target))) {
            Files.walkFileTree(path, new ArchiveFiles(path, zipOutputStream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
