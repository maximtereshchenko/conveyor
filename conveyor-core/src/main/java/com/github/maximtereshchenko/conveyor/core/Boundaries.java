package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

abstract class Boundaries<T extends Comparable<T>> {

    private final Set<T> all;

    Boundaries(Set<T> all) {
        this.all = all;
    }

    Set<T> all() {
        return all;
    }

    long checksum() {
        var checksum = new CRC32C();
        for (var element : new TreeSet<>(all)) {
            update(checksum, element);
        }
        return checksum.getValue();
    }

    abstract void update(Checksum checksum, T element);

    void update(Checksum checksum, Path path) {
        try {
            if (Files.exists(path)) {
                Files.walkFileTree(path, new ChecksumFileVisitor(checksum));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
