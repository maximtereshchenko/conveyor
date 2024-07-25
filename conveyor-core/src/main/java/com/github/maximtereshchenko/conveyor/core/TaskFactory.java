package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Path;

interface TaskFactory {

    Task task(Path directory, Properties properties, ConveyorTask conveyorTask, Tracer tracer);
}
