package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Optional;

record SchematicDependencyModel(
    String group,
    String name,
    Optional<DependencyScope> scope
) implements DependencyModel {

    @Override
    public SchematicDependencyModel override(DependencyModel base) {
        return switch (base) {
            case ArtifactDependencyModel ignored -> throw new IllegalArgumentException();
            case SchematicDependencyModel model ->
                new SchematicDependencyModel(group, name, scope.or(model::scope));
        };
    }
}
