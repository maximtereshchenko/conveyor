package com.github.maximtereshchenko.conveyor.api.port;

import java.io.OutputStream;
import java.nio.file.Path;

public interface DefinitionTranslator {

    SchematicDefinition schematicDefinition(Path path);

    ManualDefinition manualDefinition(Path path);

    void write(SchematicDefinition schematicDefinition, OutputStream outputStream);
}
