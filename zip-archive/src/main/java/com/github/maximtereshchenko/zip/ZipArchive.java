package com.github.maximtereshchenko.zip;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

public final class ZipArchive {

    private final Path path;

    public ZipArchive(Path path) {
        this.path = path;
    }

    public void extract(Path target) {
        try (var zipInputStream = new ZipInputStream(Files.newInputStream(path))) {
            for (
                var entry = zipInputStream.getNextEntry();
                entry != null;
                entry = zipInputStream.getNextEntry()
            ) {
                var file = target.resolve(entry.getName());
                Files.createDirectories(file.getParent());
                try (var outputStream = Files.newOutputStream(file)) {
                    zipInputStream.transferTo(outputStream);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
