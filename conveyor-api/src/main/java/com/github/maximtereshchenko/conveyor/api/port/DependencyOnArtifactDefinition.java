package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Optional;

public record DependencyOnArtifactDefinition(
    String name,
    Optional<String> version,
    Optional<DependencyScope> scope
) implements SchematicDependencyDefinition {}
