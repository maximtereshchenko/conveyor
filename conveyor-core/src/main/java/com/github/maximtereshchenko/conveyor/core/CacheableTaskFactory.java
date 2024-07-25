package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;

final class CacheableTaskFactory implements TaskFactory {

    private final TaskFactory original;

    CacheableTaskFactory(TaskFactory original) {
        this.original = original;
    }

    @Override
    public Task task(
        Path directory,
        Properties properties,
        ConveyorTask conveyorTask,
        Tracer tracer
    ) {
        var task = original.task(directory, properties, conveyorTask, tracer);
        return switch (conveyorTask.cache()) {
            case ENABLED -> new CacheableTask(
                task,
                new Inputs(conveyorTask.inputs()),
                new Outputs(conveyorTask.outputs()),
                new TaskCache(properties.tasksCacheDirectory().resolve(conveyorTask.name())),
                directory,
                tracer
            );
            case DISABLED -> task;
        };
    }
}
