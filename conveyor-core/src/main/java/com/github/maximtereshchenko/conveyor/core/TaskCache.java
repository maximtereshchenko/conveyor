package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class TaskCache {

    private final Path directory;

    TaskCache(Path directory) {
        this.directory = directory;
    }

    boolean changed(Inputs inputs, Outputs outputs) {
        return changed(inputsChecksumPath(), inputs.checksum()) ||
               changed(outputsChecksumPath(), outputs.checksum());
    }

    boolean restore(Inputs inputs, Outputs outputs, Path destination) {
        var source = directory.resolve(String.valueOf(inputs.checksum()));
        if (!Files.exists(source)) {
            return false;
        }
        outputs.delete();
        try {
            Files.walkFileTree(source, new CopyRecursively(source, destination));
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void store(Inputs inputs, Outputs outputs, Path root) {
        try {
            var existingDestination = Files.createDirectories(
                directory.resolve(String.valueOf(inputs.checksum()))
            );
            for (var path : outputs.paths()) {
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

    void remember(Inputs inputs, Outputs outputs) {
        write(inputsChecksumPath(), inputs.checksum());
        write(outputsChecksumPath(), outputs.checksum());
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

    private Path inputsChecksumPath() {
        return directory.resolve("inputs");
    }

    private Path outputsChecksumPath() {
        return directory.resolve("outputs");
    }
}
