package com.github.maximtereshchenko.conveyor.gson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.maximtereshchenko.conveyor.api.port.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JacksonAdapter implements DefinitionReader {

    private final ObjectMapper objectMapper;

    private JacksonAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static JacksonAdapter configured() {
        var module = new SimpleModule();
        module.addSerializer(NoExplicitTemplate.class, new NoExplicitTemplateSerializer());
        module.addDeserializer(TemplateDefinition.class, new TemplateDefinitionDeserializer());
        module.addDeserializer(DependencyDefinition.class, new DependencyDefinitionDeserializer());
        return new JacksonAdapter(
            new ObjectMapper()
                .registerModule(module)
                .findAndRegisterModules()
        );
    }

    @Override
    public SchematicDefinition schematicDefinition(Path path) {
        return read(path, SchematicDefinition.class);
    }

    @Override
    public ManualDefinition manualDefinition(Path path) {
        return read(path, ManualDefinition.class);
    }

    public void write(Path path, Object object) {
        try (var writer = Files.newBufferedWriter(path)) {
            objectMapper.writeValue(writer, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> T read(Path path, Class<T> type) {
        try (var reader = Files.newBufferedReader(path)) {
            return objectMapper.readValue(reader, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
