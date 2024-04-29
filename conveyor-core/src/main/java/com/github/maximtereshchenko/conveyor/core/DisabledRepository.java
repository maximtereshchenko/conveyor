package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

final class DisabledRepository implements Repository<Path> {

    @Override
    public boolean hasName(String name) {
        return false;
    }

    @Override
    public Optional<Path> artifact(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return Optional.empty();
    }

    @Override
    public void publish(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        Resource resource
    ) {
        throw new IllegalArgumentException();
    }
}
