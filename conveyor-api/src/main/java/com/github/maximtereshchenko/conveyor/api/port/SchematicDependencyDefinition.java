package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Optional;

public sealed interface SchematicDependencyDefinition permits DependencyOnArtifactDefinition,
    DependencyOnSchematicDefinition {

    Optional<DependencyScope> scope();
}
