package com.github.maximtereshchenko.conveyor.api.port;

import java.io.OutputStream;
import java.nio.file.Path;

public interface SchematicDefinitionTranslator {

    SchematicDefinition schematicDefinition(Path path);

    void write(SchematicDefinition schematicDefinition, OutputStream outputStream);
}
