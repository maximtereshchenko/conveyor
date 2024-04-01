package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

final class DisabledRepository implements Repository {

    @Override
    public Optional<Path> path(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return Optional.empty();
    }
}
