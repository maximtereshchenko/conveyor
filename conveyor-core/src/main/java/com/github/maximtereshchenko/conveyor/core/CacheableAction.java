package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

final class CacheableAction implements Supplier<Optional<Path>> {

    private final Supplier<Optional<Path>> original;
    private final Inputs inputs;
    private final Outputs outputs;
    private final TaskCache taskCache;
    private final Path directory;

    CacheableAction(
        Supplier<Optional<Path>> original,
        Inputs inputs,
        Outputs outputs,
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
        if (taskCache.changed(inputs, outputs)) {
            if (!taskCache.restore(inputs, directory)) {
                original.get();
                taskCache.store(inputs, outputs, directory);
            }
            taskCache.remember(inputs, outputs);
        }
        return Optional.empty();
    }
}
