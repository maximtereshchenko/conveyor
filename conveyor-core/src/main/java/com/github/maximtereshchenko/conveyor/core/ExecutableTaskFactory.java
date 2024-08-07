package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;

final class ExecutableTaskFactory implements TaskFactory {

    @Override
    public Task task(
        Path directory,
        Properties properties,
        String plugin,
        ConveyorTask conveyorTask,
        Tracer tracer
    ) {
        return new ExecutableTask(
            plugin,
            conveyorTask,
            tracer.withContext("plugin", plugin)
                .withContext("task", conveyorTask.name())
        );
    }
}
