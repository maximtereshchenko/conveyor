package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.TracingOutputLevel;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;

import java.nio.file.Path;
import java.util.List;

final class ConveyorModuleProxy implements ConveyorModule {

    private final SchematicDefinitionConverter schematicDefinitionConverter;

    ConveyorModuleProxy(SchematicDefinitionConverter schematicDefinitionConverter) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    @Override
    public void construct(Path path, List<Stage> stages) {
        new ConveyorFacade(schematicDefinitionConverter, message -> {}, TracingOutputLevel.SILENT)
            .construct(path, stages);
    }
}
