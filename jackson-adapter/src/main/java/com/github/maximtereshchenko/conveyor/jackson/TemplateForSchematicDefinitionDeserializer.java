package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.maximtereshchenko.conveyor.api.port.ManualTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.port.NoTemplate;
import com.github.maximtereshchenko.conveyor.api.port.SchematicPathTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.port.TemplateForSchematicDefinition;

import java.io.IOException;
import java.nio.file.Paths;

final class TemplateForSchematicDefinitionDeserializer extends StdDeserializer<TemplateForSchematicDefinition> {

    TemplateForSchematicDefinitionDeserializer() {
        super(TemplateForSchematicDefinition.class);
    }

    @Override
    public TemplateForSchematicDefinition deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {
        if (jsonParser.currentToken() == JsonToken.VALUE_NULL) {
            return new NoTemplate();
        }
        JsonNode node = jsonParser.readValueAsTree();
        var name = node.get("name");
        var version = node.get("version");
        if (name != null && version != null) {
            return new ManualTemplateDefinition(
                node.get("group").asText(),
                name.asText(),
                version.asText()
            );
        }
        return new SchematicPathTemplateDefinition(Paths.get(node.get("path").asText()));
    }
}
