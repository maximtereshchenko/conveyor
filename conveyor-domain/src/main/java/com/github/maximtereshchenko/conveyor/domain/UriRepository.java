package com.github.maximtereshchenko.conveyor.domain;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

abstract class UriRepository implements Repository {

    @Override
    public Optional<Path> path(
        String group,
        String name,
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return path(uri(group, name, semanticVersion, extension(classifier)), classifier);
    }

    abstract String extension(Classifier classifier);

    abstract Optional<Path> path(URI uri, Classifier classifier);

    private URI uri(String group, String name, SemanticVersion semanticVersion, String extension) {
        return URI.create(
            "%s/%s/%s/%s-%s.%s".formatted(
                group.replace('.', '/'),
                name,
                semanticVersion,
                name,
                semanticVersion,
                extension
            )
        );
    }
}
