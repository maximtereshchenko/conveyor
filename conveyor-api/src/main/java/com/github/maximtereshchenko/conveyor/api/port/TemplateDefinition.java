package com.github.maximtereshchenko.conveyor.api.port;

public sealed interface TemplateDefinition
    permits SchematicTemplateDefinition, NoTemplateDefinition {}
