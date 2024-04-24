package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Optional;

record DependencyModel(
    IdModel idModel,
    Optional<String> version,
    Optional<DependencyScope> scope
) implements ArtifactModel {

    DependencyModel override(DependencyModel base) {
        return new DependencyModel(
            idModel,
            version.or(base::version),
            scope.or(base::scope)
        );
    }
}
