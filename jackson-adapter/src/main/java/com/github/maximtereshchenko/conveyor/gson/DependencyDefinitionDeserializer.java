package com.github.maximtereshchenko.conveyor.gson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.maximtereshchenko.conveyor.api.port.DependencyOnArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.DependencyOnSchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.io.IOException;
import java.util.Optional;

final class DependencyDefinitionDeserializer extends StdDeserializer<SchematicDependencyDefinition> {

    DependencyDefinitionDeserializer() {
        super(SchematicDependencyDefinition.class);
    }

    @Override
    public SchematicDependencyDefinition deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {
        JsonNode node = jsonParser.readValueAsTree();
        var scope = scope(node);
        var schematic = node.get("schematic");
        if (schematic == null) {
            return new DependencyOnArtifactDefinition(node.get("name").asText(), version(node), scope);
        }
        return new DependencyOnSchematicDefinition(schematic.asText(), scope);
    }

    private Optional<String> version(JsonNode node) {
        var version = node.get("version");
        if (version == null || version.isNull()) {
            return Optional.empty();
        }
        return Optional.of(version.textValue());
    }

    private Optional<DependencyScope> scope(JsonNode node) {
        var scope = node.get("scope");
        if (scope == null) {
            return Optional.empty();
        }
        return Optional.of(DependencyScope.valueOf(scope.asText()));
    }
}
