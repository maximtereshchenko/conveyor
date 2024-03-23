package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.nio.file.Path;
import java.util.Optional;

final class DisabledRepository implements Repository {

    @Override
    public Optional<ManualDefinition> manualDefinition(
        String name,
        SemanticVersion semanticVersion
    ) {
        return Optional.empty();
    }

    @Override
    public Optional<Path> path(String name, SemanticVersion semanticVersion) {
        return Optional.empty();
    }
}
