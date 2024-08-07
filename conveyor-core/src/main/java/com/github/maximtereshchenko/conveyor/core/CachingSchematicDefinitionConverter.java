package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class CachingSchematicDefinitionConverter implements SchematicDefinitionConverter {

    private final SchematicDefinitionConverter original;
    private final Map<Path, SchematicDefinition> cache = new ConcurrentHashMap<>();

    CachingSchematicDefinitionConverter(SchematicDefinitionConverter original) {
        this.original = original;
    }

    @Override
    public SchematicDefinition schematicDefinition(Path path) {
        return cache.computeIfAbsent(path, original::schematicDefinition);
    }

    @Override
    public byte[] bytes(SchematicDefinition schematicDefinition) {
        return original.bytes(schematicDefinition);
    }
}
