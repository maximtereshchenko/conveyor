package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

final class NamedLocalDirectoryRepository implements Repository<Path> {

    private final LocalDirectoryRepository original;
    private final String name;

    NamedLocalDirectoryRepository(LocalDirectoryRepository original, String name) {
        this.original = original;
        this.name = name;
    }

    @Override
    public boolean hasName(String name) {
        return this.name.equals(name);
    }

    @Override
    public Optional<Path> artifact(Id id, Version version, Classifier classifier) {
        return original.artifact(id, version, classifier);
    }

    @Override
    public void publish(
        Id id,
        Version version,
        Classifier classifier,
        Resource resource
    ) {
        original.publish(id, version, classifier, resource);
    }
}
