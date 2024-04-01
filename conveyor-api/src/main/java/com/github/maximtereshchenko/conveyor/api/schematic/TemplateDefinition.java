package com.github.maximtereshchenko.conveyor.api.schematic;

public sealed interface TemplateDefinition
    permits SchematicTemplateDefinition, NoTemplateDefinition {}
