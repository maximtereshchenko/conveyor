package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.maximtereshchenko.conveyor.api.port.NoTemplate;

import java.io.IOException;

final class NoExplicitlyDefinedTemplateSerializer extends StdSerializer<NoTemplate> {

    NoExplicitlyDefinedTemplateSerializer() {
        super(NoTemplate.class);
    }

    @Override
    public void serialize(
        NoTemplate definition,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeNull();
    }
}
