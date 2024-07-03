package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.List;

final class ConveyorModuleProxy implements ConveyorModule {

    private final SchematicDefinitionConverter schematicDefinitionConverter;

    ConveyorModuleProxy(SchematicDefinitionConverter schematicDefinitionConverter) {
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    @Override
    public void construct(Path path, List<Stage> stages) {
        new ConveyorFacade(schematicDefinitionConverter).construct(path, stages);
    }
}
