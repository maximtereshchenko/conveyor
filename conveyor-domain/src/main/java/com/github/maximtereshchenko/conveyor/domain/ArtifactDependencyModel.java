package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Optional;

record ArtifactDependencyModel(String name, Optional<Integer> version, Optional<DependencyScope> scope)
    implements DependencyModel {

    @Override
    public ArtifactDependencyModel override(DependencyModel base) {
        return switch (base) {
            case ArtifactDependencyModel model ->
                new ArtifactDependencyModel(name, version.or(model::version), scope.or(model::scope));
            case SchematicDependencyModel ignored -> throw new IllegalArgumentException();
        };
    }
}
