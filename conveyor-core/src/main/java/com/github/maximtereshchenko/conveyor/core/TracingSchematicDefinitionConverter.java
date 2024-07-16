package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;

import java.nio.file.Path;

final class TracingSchematicDefinitionConverter implements SchematicDefinitionConverter {

    private final SchematicDefinitionConverter original;
    private final Tracer tracer;

    TracingSchematicDefinitionConverter(SchematicDefinitionConverter original, Tracer tracer) {
        this.original = original;
        this.tracer = tracer;
    }

    @Override
    public SchematicDefinition schematicDefinition(Path path) {
        var schematicDefinition = original.schematicDefinition(path);
        tracer.submitSchematicDefinition(schematicDefinition, path);
        return schematicDefinition;
    }

    @Override
    public byte[] bytes(SchematicDefinition schematicDefinition) {
        return original.bytes(schematicDefinition);
    }
}
