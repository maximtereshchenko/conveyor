package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.NoTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.RepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.TemplateDefinition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JacksonAdapter implements SchematicDefinitionConverter {

    private final ObjectMapper objectMapper;

    private JacksonAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static JacksonAdapter configured() {
        var module = new SimpleModule();
        module.addSerializer(Path.class, new ToStringSerializer());
        module.addSerializer(NoTemplateDefinition.class, new NoTemplateDefinitionSerializer());
        module.addDeserializer(TemplateDefinition.class, new TemplateDefinitionDeserializer());
        module.addDeserializer(
            RepositoryDefinition.class,
            new RepositoryDefinitionDeserializer()
        );
        return new JacksonAdapter(
            new ObjectMapper()
                .registerModule(module)
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        );
    }

    @Override
    public SchematicDefinition schematicDefinition(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            return objectMapper.readValue(reader, SchematicDefinition.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] bytes(SchematicDefinition schematicDefinition) {
        try {
            return objectMapper.writeValueAsBytes(schematicDefinition);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
