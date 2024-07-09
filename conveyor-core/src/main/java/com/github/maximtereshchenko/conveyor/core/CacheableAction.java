package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;

final class CacheableAction implements Runnable {

    private final Runnable original;
    private final Inputs inputs;
    private final Outputs outputs;
    private final TaskCache taskCache;
    private final Path directory;

    CacheableAction(
        Runnable original,
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
    public void run() {
        if (taskCache.changed(inputs, outputs)) {
            if (!taskCache.restore(inputs, outputs, directory)) {
                original.run();
                taskCache.store(inputs, outputs, directory);
            }
            taskCache.remember(inputs, outputs);
        }
    }
}
