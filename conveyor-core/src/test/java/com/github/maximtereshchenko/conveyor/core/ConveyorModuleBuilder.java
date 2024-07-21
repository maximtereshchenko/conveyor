package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final class ConveyorModuleBuilder {

    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final Executor executor;

    ConveyorModuleBuilder(
        SchematicDefinitionConverter schematicDefinitionConverter,
        Executor executor
    ) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.executor = executor;
    }

    ConveyorModuleBuilder parallel() {
        return new ConveyorModuleBuilder(
            schematicDefinitionConverter,
            Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    ConveyorModule build() {
        return new ConveyorModuleProxy(schematicDefinitionConverter, executor);
    }
}
