package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

abstract class UriRepository implements Repository {

    @Override
    public Optional<Path> path(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return path(uri(id, semanticVersion, extension(classifier)), classifier);
    }

    abstract Optional<Path> path(URI uri, Classifier classifier);

    private String extension(Classifier classifier) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> "json";
            case MODULE -> "jar";
        };
    }

    private URI uri(Id id, SemanticVersion semanticVersion, String extension) {
        return URI.create(
            "%s/%s/%s/%s-%s.%s".formatted(
                id.group().replace('.', '/'),
                id.name(),
                semanticVersion,
                id.name(),
                semanticVersion,
                extension
            )
        );
    }
}
