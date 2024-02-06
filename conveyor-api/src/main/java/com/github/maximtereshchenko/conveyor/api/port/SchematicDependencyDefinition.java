package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Objects;

public record SchematicDependencyDefinition(String schematic, DependencyScope scope) implements DependencyDefinition {

    public SchematicDependencyDefinition {
        scope = Objects.requireNonNullElse(scope, DependencyScope.IMPLEMENTATION);
    }
}
