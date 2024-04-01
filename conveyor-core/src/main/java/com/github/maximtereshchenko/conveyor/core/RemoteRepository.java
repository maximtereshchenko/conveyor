package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionTranslator;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

final class RemoteRepository extends UriRepository {

    private final LocalDirectoryRepository cache;
    private final Http http;
    private final SchematicDefinitionFactory schematicDefinitionFactory;
    private final SchematicDefinitionTranslator schematicDefinitionTranslator;
    private final URL url;

    RemoteRepository(
        LocalDirectoryRepository cache,
        Http http,
        SchematicDefinitionFactory schematicDefinitionFactory,
        SchematicDefinitionTranslator schematicDefinitionTranslator,
        URL url
    ) {
        this.cache = cache;
        this.http = http;
        this.schematicDefinitionFactory = schematicDefinitionFactory;
        this.schematicDefinitionTranslator = schematicDefinitionTranslator;
        this.url = url;
    }

    @Override
    String extension(Classifier classifier) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> "pom";
            case MODULE -> "jar";
        };
    }

    @Override
    Optional<Path> path(URI uri, Classifier classifier) {
        return cache.path(uri, classifier).or(() -> cachedPath(uri, classifier));
    }

    private Optional<Path> cachedPath(URI uri, Classifier classifier) {
        store(uri, classifier);
        return cache.path(uri, classifier);
    }

    private void store(URI uri, Classifier classifier) {
        switch (classifier) {
            case SCHEMATIC_DEFINITION -> storeSchematicDefinition(uri);
            case MODULE -> storeModule(uri);
        }
    }

    private void storeModule(URI uri) {
        http.get(absoluteUri(uri), inputStream -> cache.stored(uri, inputStream::transferTo));
    }

    private void storeSchematicDefinition(URI uri) {
        http.get(
            absoluteUri(uri),
            inputStream -> cache.stored(
                uri,
                outputStream -> schematicDefinitionTranslator.write(
                    schematicDefinitionFactory.schematicDefinition(inputStream),
                    outputStream
                )
            )
        );
    }

    private URI absoluteUri(URI uri) {
        try {
            return url.toURI().resolve(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
