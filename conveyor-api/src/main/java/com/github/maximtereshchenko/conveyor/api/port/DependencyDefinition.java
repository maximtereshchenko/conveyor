package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

public sealed interface DependencyDefinition permits ArtifactDependencyDefinition, SchematicDependencyDefinition {

    DependencyScope scope();
}
