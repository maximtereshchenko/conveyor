package com.github.maximtereshchenko.conveyor.api.schematic;

public record SchematicTemplateDefinition(String group, String name, String version)
    implements TemplateDefinition {}
