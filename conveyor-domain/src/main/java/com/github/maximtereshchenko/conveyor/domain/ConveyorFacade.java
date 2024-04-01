package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;

public final class ConveyorFacade implements ConveyorModule {

    private final DefinitionReader definitionReader;

    public ConveyorFacade(DefinitionReader definitionReader) {
        this.definitionReader = definitionReader;
    }

    @Override
    public SchematicProducts construct(Path path, Stage stage) {
        return Schematics.from(Schematic.from(definitionReader, path)).construct(stage);
    }
}
