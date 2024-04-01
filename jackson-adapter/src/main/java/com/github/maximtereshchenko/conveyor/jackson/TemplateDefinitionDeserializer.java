package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.maximtereshchenko.conveyor.api.schematic.NoTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.TemplateDefinition;

import java.io.IOException;

final class TemplateDefinitionDeserializer extends StdDeserializer<TemplateDefinition> {

    TemplateDefinitionDeserializer() {
        super(TemplateDefinition.class);
    }

    @Override
    public TemplateDefinition deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {
        if (jsonParser.currentToken() == JsonToken.VALUE_NULL) {
            return new NoTemplateDefinition();
        }
        JsonNode node = jsonParser.readValueAsTree();
        return new SchematicTemplateDefinition(
            node.get("group").asText(),
            node.get("name").asText(),
            node.get("version").asText()
        );
    }
}
