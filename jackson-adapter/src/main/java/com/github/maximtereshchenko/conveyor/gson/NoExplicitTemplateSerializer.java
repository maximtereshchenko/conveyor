package com.github.maximtereshchenko.conveyor.gson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.maximtereshchenko.conveyor.api.port.NoExplicitTemplate;

import java.io.IOException;

final class NoExplicitTemplateSerializer extends StdSerializer<NoExplicitTemplate> {

    NoExplicitTemplateSerializer() {
        super(NoExplicitTemplate.class);
    }

    @Override
    public void serialize(
        NoExplicitTemplate definition,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeNull();
    }
}
