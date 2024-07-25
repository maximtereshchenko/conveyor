package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.TaskCache;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final class ConveyorModuleBuilder {

    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final Executor executor;
    private final TaskCache taskCache;

    ConveyorModuleBuilder(
        SchematicDefinitionConverter schematicDefinitionConverter,
        Executor executor,
        TaskCache taskCache
    ) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.executor = executor;
        this.taskCache = taskCache;
    }

    ConveyorModuleBuilder parallel() {
        return new ConveyorModuleBuilder(
            schematicDefinitionConverter,
            Executors.newVirtualThreadPerTaskExecutor(),
            taskCache
        );
    }

    ConveyorModuleBuilder disabledTaskCache() {
        return new ConveyorModuleBuilder(
            schematicDefinitionConverter,
            executor,
            TaskCache.DISABLED
        );
    }

    ConveyorModule build() {
        return new ConveyorModuleProxy(schematicDefinitionConverter, executor, taskCache);
    }
}
