package com.github.maximtereshchenko.conveyor.api.port;

public sealed interface TemplateForSchematicDefinition
    permits ManualTemplateDefinition, NoTemplate, SchematicPathTemplateDefinition {}
