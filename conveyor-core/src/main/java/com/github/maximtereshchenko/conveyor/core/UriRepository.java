package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;
import java.util.Optional;

abstract class UriRepository<I, O> implements Repository<I, O> {

    private final URI base;

    UriRepository(URI base) {
        this.base = base;
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        I artifact
    ) {
        publish(uri(id, version, classifier), artifact);
    }

    @Override
    public Optional<O> artifact(Id id, Version version, Classifier classifier) {
        return artifact(uri(id, version, classifier));
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

    abstract void publish(URI uri, I artifact);

    abstract Optional<O> artifact(URI uri);
}
