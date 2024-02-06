package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;

public final class ConveyorFacade implements ConveyorModule {

    private final SchematicFactory schematicFactory;

    public ConveyorFacade(DefinitionReader definitionReader) {
        this.schematicFactory = new SchematicFactory(definitionReader);
    }

    @Override
    public SchematicProducts construct(Path path, Stage stage) {
        return schematicFactory.schematics(path)
            .stream()
            .reduce(
                new SchematicProducts(),
                (schematicProducts, schematic) -> schematic.construct(schematicProducts, stage),
                new PickSecond<>()
            );
    }
}
