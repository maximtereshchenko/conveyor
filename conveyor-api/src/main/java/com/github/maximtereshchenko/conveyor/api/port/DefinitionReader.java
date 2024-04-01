package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;

public interface DefinitionReader {

    SchematicDefinition schematicDefinition(Path path);

    ManualDefinition manualDefinition(Path path);
}
