package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import java.util.Objects;

record ProjectDependencyDefinition(String name, int version, DependencyScope scope) implements ArtifactDefinition {

    ProjectDependencyDefinition {
        scope = Objects.requireNonNullElse(scope, DependencyScope.IMPLEMENTATION);
    }
}
