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
    private final TaskCache taskCache;
    private final Path directory;

    CacheableAction(
        Supplier<Optional<Path>> original,
        Set<Path> inputs,
        Set<Path> outputs,
        TaskCache taskCache,
        Path directory
    ) {
        this.original = original;
        this.inputs = inputs;
        this.outputs = outputs;
        this.taskCache = taskCache;
        this.directory = directory;
    }

    @Override
    public Optional<Path> get() {
        try {
            var inputsChecksum = checksum(inputs);
            if (inputsOutputsChanged(inputsChecksum)) {
                if (!taskCache.restore(inputsChecksum, directory)) {
                    original.get();
                    taskCache.store(inputsChecksum, outputs, directory);
                }
                taskCache.remember(inputsChecksum, checksum(outputs));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean inputsOutputsChanged(long inputsChecksum) throws IOException {
        return taskCache.inputsChanged(inputsChecksum) ||
               taskCache.outputsChanged(checksum(outputs));
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
