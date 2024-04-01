package com.github.maximtereshchenko.conveyor.api.port;

public record SchematicTemplateDefinition(String group, String name, String version)
    implements TemplateDefinition {}
