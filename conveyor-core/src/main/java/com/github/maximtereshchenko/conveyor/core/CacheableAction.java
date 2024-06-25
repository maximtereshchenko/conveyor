package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

final class CacheableAction implements Supplier<Optional<Path>> {

    private final Supplier<Optional<Path>> original;
    private final Set<Path> inputs;
    private final Set<Path> outputs;
    private final Path inputsChecksumLocation;
    private final Path outputsChecksumLocation;
    private final Path cacheDirectory;
    private final Path directory;

    CacheableAction(
        Supplier<Optional<Path>> original,
        Set<Path> inputs,
        Set<Path> outputs,
        Path inputsChecksumLocation,
        Path outputsChecksumLocation,
        Path cacheDirectory,
        Path directory
    ) {
        this.original = original;
        this.inputs = inputs;
        this.outputs = outputs;
        this.inputsChecksumLocation = inputsChecksumLocation;
        this.outputsChecksumLocation = outputsChecksumLocation;
        this.cacheDirectory = cacheDirectory;
        this.directory = directory;
    }

    @Override
    public Optional<Path> get() {
        try {
            var inputsChecksum = checksum(inputs);
            if (changed(inputsChecksumLocation, inputsChecksum) ||
                changed(outputsChecksumLocation, checksum(outputs))) {
                var path = Optional.<Path>empty();
                var cachedOutputsDirectory = cacheDirectory.resolve(String.valueOf(inputsChecksum));
                if (Files.exists(cachedOutputsDirectory)) {
                    restore(cachedOutputsDirectory);
                } else {
                    path = original.get();
                    cache(cachedOutputsDirectory);
                }
                write(inputsChecksumLocation, inputsChecksum);
                write(outputsChecksumLocation, checksum(outputs));
                return path;
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void restore(Path source) throws IOException {
        Files.walkFileTree(source, new CopyRecursively(source, directory));
    }

    private void cache(Path destination) throws IOException {
        var existingDestination = Files.createDirectories(destination);
        for (var path : outputs) {
            if (Files.exists(path)) { //TODO test
                var to = existingDestination.resolve(directory.relativize(path));
                if (Files.isRegularFile(path)) {
                    Files.createDirectories(to.getParent()); //TODO
                }
                Files.walkFileTree(
                    path,
                    new CopyRecursively(path, to)
                );
            }
        }
    }

    private void write(Path path, long checksum) throws IOException {
        Files.writeString(path, String.valueOf(checksum));
    }

    private boolean changed(Path path, long expected) throws IOException {
        return checksum(path).map(previous -> previous != expected)
            .orElse(Boolean.TRUE);
    }

    private Optional<Long> checksum(Path path) throws IOException {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(Files.readString(path)));
    }

    private long checksum(Set<Path> paths) throws IOException {
        var visitor = new ChecksumFileVisitor();
        for (var path : paths) {
            if (Files.exists(path)) {
                Files.walkFileTree(path, visitor);
            }
        }
        return visitor.checksum();
    }
}
