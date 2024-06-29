package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class TaskCache {

    private final Path directory;

    TaskCache(Path directory) {
        this.directory = directory;
    }

    boolean inputsChanged(long expected) {
        return changed(inputs(), expected);
    }

    boolean outputsChanged(long expected) {
        return changed(outputs(), expected);
    }

    boolean restore(long checksum, Path destination) {
        var source = directory.resolve(String.valueOf(checksum));
        if (!Files.exists(source)) {
            return false;
        }
        try {
            Files.walkFileTree(source, new CopyRecursively(source, destination));
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void store(long checksum, Set<Path> outputs, Path root) {
        try {
            var existingDestination = Files.createDirectories(
                directory.resolve(String.valueOf(checksum))
            );
            for (var path : outputs) {
                if (Files.exists(path)) { //TODO test
                    var to = existingDestination.resolve(root.relativize(path));
                    if (Files.isRegularFile(path)) {
                        Files.createDirectories(to.getParent()); //TODO
                    }
                    Files.walkFileTree(path, new CopyRecursively(path, to));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void remember(long inputsChecksum, long outputsChecksum) {
        write(inputs(), inputsChecksum);
        write(outputs(), outputsChecksum);
    }

    private boolean changed(Path path, long expected) {
        if (!Files.exists(path)) {
            return true;
        }
        try {
            return Long.parseLong(Files.readString(path)) != expected;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(Path path, long checksum) {
        try {
            Files.writeString(path, String.valueOf(checksum));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path inputs() {
        return directory.resolve("inputs");
    }

    private Path outputs() {
        return directory.resolve("outputs");
    }
}
