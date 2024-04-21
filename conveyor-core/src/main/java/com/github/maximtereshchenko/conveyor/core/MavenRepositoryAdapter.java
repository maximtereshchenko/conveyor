package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.api.schematic.SchematicDefinition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class MavenRepositoryAdapter implements UriRepository<InputStream> {

    private final UriRepository<Path> original;
    private final SchematicDefinitionFactory schematicDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;

    MavenRepositoryAdapter(
        UriRepository<Path> original,
        SchematicDefinitionFactory schematicDefinitionFactory,
        SchematicDefinitionConverter schematicDefinitionConverter
    ) {
        this.original = original;
        this.schematicDefinitionFactory = schematicDefinitionFactory;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
    }

    @Override
    public Optional<InputStream> artifact(URI uri) {
        if (uri.getPath().endsWith(".json")) {
            return original.artifact(
                    URI.create(uri.toString().replaceAll("\\.json$", ".pom"))
                )
                .map(this::schematicDefinition)
                .map(this::inputStream);
        }
        return original.artifact(uri)
            .map(this::inputStream);
    }

    private InputStream inputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private InputStream inputStream(SchematicDefinition schematicDefinition) {
        return new ByteArrayInputStream(schematicDefinitionConverter.bytes(schematicDefinition));
    }

    private SchematicDefinition schematicDefinition(Path path) {
        try (var inputStream = Files.newInputStream(path)) {
            return schematicDefinitionFactory.schematicDefinition(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
