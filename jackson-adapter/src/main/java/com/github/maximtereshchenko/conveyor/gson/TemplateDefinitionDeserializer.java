package com.github.maximtereshchenko.conveyor.gson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.maximtereshchenko.conveyor.api.port.ManualTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.port.NoExplicitTemplate;
import com.github.maximtereshchenko.conveyor.api.port.TemplateDefinition;

import java.io.IOException;

final class TemplateDefinitionDeserializer extends StdDeserializer<TemplateDefinition> {

    TemplateDefinitionDeserializer() {
        super(TemplateDefinition.class);
    }

    @Override
    public TemplateDefinition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {
        if (jsonParser.currentToken() == JsonToken.VALUE_NULL) {
            return new NoExplicitTemplate();
        }
        JsonNode node = jsonParser.readValueAsTree();
        return new ManualTemplateDefinition(node.get("name").asText(), node.get("version").intValue());
    }
}
