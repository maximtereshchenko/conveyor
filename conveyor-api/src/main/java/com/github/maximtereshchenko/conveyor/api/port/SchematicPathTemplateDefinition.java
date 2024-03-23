package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;

public record SchematicPathTemplateDefinition(Path path)
    implements TemplateForSchematicDefinition {}
