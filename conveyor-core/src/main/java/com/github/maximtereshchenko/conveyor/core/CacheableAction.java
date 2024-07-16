package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;

import java.nio.file.Path;

final class CacheableAction implements ConveyorTaskAction {

    private final ConveyorTaskAction original;
    private final Inputs inputs;
    private final Outputs outputs;
    private final TaskCache taskCache;
    private final Path directory;
    private final Tracer tracer;

    CacheableAction(
        ConveyorTaskAction original,
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
    public void execute(ConveyorTaskTracer conveyorTaskTracer) {
        if (taskCache.changed(inputs, outputs)) {
            if (taskCache.restore(inputs, outputs, directory)) {
                tracer.submitTaskRestoredFromCache();
            } else {
                original.execute(conveyorTaskTracer);
                taskCache.store(inputs, outputs, directory);
            }
            taskCache.remember(inputs, outputs);
        }
        tracer.submitTaskUpToDate();
    }
}
