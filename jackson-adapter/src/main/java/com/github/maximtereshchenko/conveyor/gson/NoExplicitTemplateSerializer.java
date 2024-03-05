package com.github.maximtereshchenko.conveyor.gson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.maximtereshchenko.conveyor.api.port.NoExplicitlyDefinedTemplate;

import java.io.IOException;

final class NoExplicitTemplateSerializer extends StdSerializer<NoExplicitlyDefinedTemplate> {

    NoExplicitTemplateSerializer() {
        super(NoExplicitlyDefinedTemplate.class);
    }

    @Override
    public void serialize(
        NoExplicitlyDefinedTemplate definition,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeNull();
    }
}
