package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;
import java.util.Optional;

abstract class UriRepository<T> implements Repository<T> {

    private final URI base;

    UriRepository(URI base) {
        this.base = base;
    }

    @Override
    public Optional<T> artifact(Id id, Version version, Classifier classifier) {
        return artifact(uri(id, version, classifier));
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Resource resource
    ) {
        publish(uri(id, version, classifier), resource);
    }

    URI uri(Id id, Version version, Classifier classifier) {
        return URI.create(
            "%s/%s/%s/%s/%s-%s.%s".formatted(
                base,
                id.group().replace('.', '/'),
                id.name(),
                version,
                id.name(),
                version,
                switch (classifier) {
                    case SCHEMATIC_DEFINITION -> "json";
                    case JAR -> "jar";
                    case POM -> "pom";
                }
            )
        );
    }

    abstract Optional<T> artifact(URI uri);

    abstract void publish(URI uri, Resource resource);
}
