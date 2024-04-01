package com.github.maximtereshchenko.conveyor.gson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.maximtereshchenko.conveyor.api.port.ArtifactDependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.io.IOException;

final class DependencyDefinitionDeserializer extends StdDeserializer<DependencyDefinition> {

    DependencyDefinitionDeserializer() {
        super(DependencyDefinition.class);
    }

    @Override
    public DependencyDefinition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {
        JsonNode node = jsonParser.readValueAsTree();
        var scope = DependencyScope.valueOf(node.get("scope").asText());
        var schematic = node.get("schematic");
        if (schematic == null) {
            return new ArtifactDependencyDefinition(node.get("name").asText(), node.get("version").intValue(), scope);
        }
        return new SchematicDependencyDefinition(schematic.asText(), scope);
    }
}
