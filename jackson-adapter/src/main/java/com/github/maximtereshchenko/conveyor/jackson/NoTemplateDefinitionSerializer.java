package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.maximtereshchenko.conveyor.api.schematic.NoTemplateDefinition;

import java.io.IOException;

final class NoTemplateDefinitionSerializer extends StdSerializer<NoTemplateDefinition> {

    NoTemplateDefinitionSerializer() {
        super(NoTemplateDefinition.class);
    }

    @Override
    public void serialize(
        NoTemplateDefinition definition,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeNull();
    }
}
