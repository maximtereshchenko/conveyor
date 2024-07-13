package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.DependencyScope;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

record DependencyModel(
    IdModel idModel,
    Optional<String> version,
    Optional<DependencyScope> scope,
    Set<Id> exclusions
) implements ArtifactModel {

    DependencyModel override(DependencyModel base) {
        var copy = new HashSet<>(base.exclusions());
        copy.addAll(exclusions);
        return new DependencyModel(
            idModel,
            version.or(base::version),
            scope.or(base::scope),
            copy
        );
    }
}
