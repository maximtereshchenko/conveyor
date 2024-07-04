package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.filevisitors.Generic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

abstract class Boundaries<T extends Comparable<T>> {

    long checksum(Set<T> elements) {
        var checksum = new CRC32C();
        for (var element : new TreeSet<>(elements)) {
            update(checksum, element);
        }
        return checksum.getValue();
    }

    void update(Checksum checksum, Path path) {
        try {
            if (Files.exists(path)) {
                Files.walkFileTree(path, new Generic(file -> updateForFile(checksum, file)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    abstract long checksum();

    abstract void update(Checksum checksum, T element);

    private void updateForFile(Checksum checksum, Path file) throws IOException {
        checksum.update(file.toString().getBytes(StandardCharsets.UTF_8));
        checksum.update(Files.readAllBytes(file));
    }
}
