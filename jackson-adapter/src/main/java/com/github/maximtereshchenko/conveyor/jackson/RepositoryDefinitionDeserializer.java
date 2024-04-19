package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.maximtereshchenko.conveyor.api.schematic.LocalDirectoryRepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.RemoteRepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.RepositoryDefinition;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

final class RepositoryDefinitionDeserializer extends StdDeserializer<RepositoryDefinition> {

    RepositoryDefinitionDeserializer() {
        super(RepositoryDefinition.class);
    }

    @Override
    public RepositoryDefinition deserialize(JsonParser jsonParser, DeserializationContext context)
        throws IOException {
        JsonNode jsonNode = jsonParser.readValueAsTree();
        var name = jsonNode.get("name").asText();
        var enabled = enabled(jsonNode);
        var path = jsonNode.get("path");
        if (path == null) {
            return new RemoteRepositoryDefinition(
                name,
                URI.create(jsonNode.get("uri").asText()),
                enabled
            );
        }
        return new LocalDirectoryRepositoryDefinition(
            name,
            context.readTreeAsValue(path, Path.class),
            enabled
        );
    }

    private Optional<Boolean> enabled(JsonNode jsonNode) {
        var node = jsonNode.get("enabled");
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        return Optional.of(node.asBoolean());
    }
}
