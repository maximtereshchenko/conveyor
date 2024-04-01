package com.github.maximtereshchenko.conveyor.gson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.maximtereshchenko.conveyor.api.port.LocalDirectoryRepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.port.RemoteRepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.port.RepositoryDefinition;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

final class RepositoryDefinitionDeserializer extends StdDeserializer<RepositoryDefinition> {

    RepositoryDefinitionDeserializer() {
        super(RepositoryDefinition.class);
    }

    @Override
    public RepositoryDefinition deserialize(JsonParser jsonParser, DeserializationContext context)
        throws IOException, JacksonException {
        JsonNode jsonNode = jsonParser.readValueAsTree();
        var name = jsonNode.get("name").asText();
        var enabled = enabled(jsonNode);
        var path = jsonNode.get("path");
        if (path == null) {
            return new RemoteRepositoryDefinition(
                name,
                URI.create(jsonNode.get("url").asText()).toURL(),
                enabled
            );
        }
        return new LocalDirectoryRepositoryDefinition(name, Paths.get(path.asText()), enabled);
    }

    private Optional<Boolean> enabled(JsonNode jsonNode) {
        var node = jsonNode.get("enabled");
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        return Optional.of(node.asBoolean());
    }
}
