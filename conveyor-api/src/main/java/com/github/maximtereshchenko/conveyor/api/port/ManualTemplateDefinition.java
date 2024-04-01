package com.github.maximtereshchenko.conveyor.api.port;

public record ManualTemplateDefinition(String name, int version)
    implements TemplateForManualDefinition, TemplateForSchematicDefinition {}
