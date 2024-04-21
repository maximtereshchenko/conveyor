package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;

import java.nio.file.Path;

public interface SchematicDefinitionConverter {

    SchematicDefinition schematicDefinition(Path path);

    byte[] bytes(SchematicDefinition schematicDefinition);
}
