package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Objects;

public record ArtifactDependencyDefinition(String name, int version, DependencyScope scope)
    implements DependencyDefinition, ArtifactDefinition {

    public ArtifactDependencyDefinition {
        scope = Objects.requireNonNullElse(scope, DependencyScope.IMPLEMENTATION);
    }
}
