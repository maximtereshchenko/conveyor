package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;
import java.util.Optional;

abstract class UriRepository<T> implements Repository<T> {

    private final URI base;

    UriRepository(URI base) {
        this.base = base;
    }

    @Override
    public Optional<T> artifact(Id id, SemanticVersion semanticVersion, Classifier classifier) {
        return artifact(uri(id, semanticVersion, classifier));
    }

    URI uri(Id id, SemanticVersion semanticVersion, Classifier classifier) {
        return URI.create(
            "%s/%s/%s/%s/%s-%s.%s".formatted(
                base,
                id.group().replace('.', '/'),
                id.name(),
                semanticVersion,
                id.name(),
                semanticVersion,
                switch (classifier) {
                    case SCHEMATIC_DEFINITION -> "json";
                    case MODULE -> "jar";
                    case POM -> "pom";
                }
            )
        );
    }

    abstract Optional<T> artifact(URI uri);
}
