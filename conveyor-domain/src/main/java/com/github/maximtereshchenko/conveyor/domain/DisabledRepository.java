package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Optional;

final class DisabledRepository implements Repository {

    @Override
    public Optional<Path> path(
        String group,
        String name,
        SemanticVersion semanticVersion,
        Classifier classifier
    ) {
        return Optional.empty();
    }
}
