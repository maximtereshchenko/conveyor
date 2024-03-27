package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.nio.file.Path;
import java.util.Optional;

interface Repository {

    Optional<ManualDefinition> manualDefinition(
        String group,
        String name,
        SemanticVersion semanticVersion
    );

    Optional<Path> path(
        String group,
        String name,
        SemanticVersion semanticVersion
    );
}
