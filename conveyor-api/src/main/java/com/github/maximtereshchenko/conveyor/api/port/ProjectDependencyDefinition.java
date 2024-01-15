package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import java.util.Objects;

public record ProjectDependencyDefinition(String name, int version, DependencyScope scope)
    implements ArtifactDefinition {

    public ProjectDependencyDefinition {
        scope = Objects.requireNonNullElse(scope, DependencyScope.IMPLEMENTATION);
    }
}
