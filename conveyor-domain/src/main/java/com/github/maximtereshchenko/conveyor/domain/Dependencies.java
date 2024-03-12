package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class Dependencies {

    private final Set<Dependency> all;

    Dependencies(Set<Dependency> all) {
        this.all = all;
    }

    Set<Path> modulePath(Set<DependencyScope> scopes) {
        return ModulePath.from(
                all.stream()
                    .filter(dependency -> scopes.contains(dependency.scope()))
                    .collect(Collectors.toSet())
            )
            .resolved();
    }
}
