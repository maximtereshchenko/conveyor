package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;

public interface DefinitionTranslator {

    SchematicDefinition schematicDefinition(Path path);

    ManualDefinition manualDefinition(Path path);

    void write(ManualDefinition manualDefinition, Path path);
}
