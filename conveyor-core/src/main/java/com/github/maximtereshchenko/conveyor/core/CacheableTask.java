package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStep;

import java.nio.file.Path;

final class CacheableTask implements Task {

    private final Task original;
    private final Inputs inputs;
    private final Outputs outputs;
    private final TaskCache taskCache;
    private final Path directory;
    private final Tracer tracer;

    CacheableTask(
        Task original,
        Inputs inputs,
        Outputs outputs,
        TaskCache taskCache,
        Path directory,
        Tracer tracer
    ) {
        this.original = original;
        this.inputs = inputs;
        this.outputs = outputs;
        this.taskCache = taskCache;
        this.directory = directory;
        this.tracer = tracer;
    }

    @Override
    public String name() {
        return original.name();
    }

    @Override
    public BindingStage stage() {
        return original.stage();
    }

    @Override
    public BindingStep step() {
        return original.step();
    }

    @Override
    public void execute() {
        if (taskCache.changed(inputs, outputs)) {
            outputs.delete();
            if (taskCache.restore(inputs, directory)) {
                tracer.submitTaskRestoredFromCache(original.name());
            } else {
                original.execute();
                taskCache.store(inputs, outputs, directory);
            }
            taskCache.remember(inputs, outputs);
        }
        tracer.submitTaskUpToDate(original.name());
    }

    @Override
    public int compareTo(Task task) {
        return original.compareTo(task);
    }
}
