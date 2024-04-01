package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class Dependencies {

    private final Set<Dependency> all;
    private final ModulePathFactory modulePathFactory;

    Dependencies(Set<Dependency> all, ModulePathFactory modulePathFactory) {
        this.all = all;
        this.modulePathFactory = modulePathFactory;
    }

    Set<Path> modulePath(Set<DependencyScope> scopes) {
        return modulePathFactory.modulePath(
            all.stream()
                .filter(dependency -> scopes.contains(dependency.scope()))
                .collect(Collectors.toSet())
        );
    }
}
