package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.TaskCache;
import com.github.maximtereshchenko.conveyor.api.TracingOutputLevel;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;

final class ConveyorModuleProxy implements ConveyorModule {

    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final Executor executor;
    private final TaskCache taskCache;

    ConveyorModuleProxy(
        SchematicDefinitionConverter schematicDefinitionConverter,
        Executor executor,
        TaskCache taskCache
    ) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.executor = executor;
        this.taskCache = taskCache;
    }

    @Override
    public void construct(Path path, List<Stage> stages) {
        ConveyorFacade.from(
                schematicDefinitionConverter,
                executor,
                taskCache,
                message -> {},
                TracingOutputLevel.SILENT
            )
            .construct(path, stages);
    }
}
