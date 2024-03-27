package com.github.maximtereshchenko.conveyor.api.port;

public record ManualTemplateDefinition(String group, String name, String version)
    implements TemplateForManualDefinition, TemplateForSchematicDefinition {}
