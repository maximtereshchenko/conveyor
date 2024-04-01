package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Optional;

record DependencyModel(
    String group,
    String name,
    Optional<String> version,
    Optional<DependencyScope> scope
) implements ArtifactModel {

    DependencyModel override(DependencyModel base) {
        return new DependencyModel(
            group,
            name,
            version.or(base::version),
            scope.or(base::scope)
        );
    }
}
