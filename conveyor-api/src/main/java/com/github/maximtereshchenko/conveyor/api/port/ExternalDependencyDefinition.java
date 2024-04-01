package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Objects;

public record ExternalDependencyDefinition(String name, int version, DependencyScope scope)
    implements ArtifactDefinition, DependencyDefinition {

    public ExternalDependencyDefinition {
        scope = Objects.requireNonNullElse(scope, DependencyScope.IMPLEMENTATION);
    }
}
