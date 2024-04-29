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
    public Optional<Path> artifact(Id id, SemanticVersion semanticVersion, Classifier classifier) {
        return original.artifact(id, semanticVersion, classifier);
    }

    @Override
    public void publish(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        Resource resource
    ) {
        original.publish(id, semanticVersion, classifier, resource);
    }
}
