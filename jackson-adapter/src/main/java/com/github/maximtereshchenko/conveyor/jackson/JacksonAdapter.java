package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.NoTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.RepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.TemplateDefinition;
import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.io.UncheckedIOException;
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
            JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(module)
                .findAndAddModules()
                .build()
        );
    }

    @Override
    public SchematicDefinition schematicDefinition(Path path) {
        return new FileTree(path)
            .read(inputStream -> objectMapper.readValue(inputStream, SchematicDefinition.class));
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
