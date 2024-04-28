package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class Dependencies {

    private final Set<Dependency> all;
    private final ClassPathFactory classPathFactory;

    Dependencies(Set<Dependency> all, ClassPathFactory classPathFactory) {
        this.all = all;
        this.classPathFactory = classPathFactory;
    }

    Set<Path> classPath(Set<DependencyScope> scopes) {
        return classPathFactory.classPath(
            all.stream()
                .filter(dependency -> scopes.contains(dependency.scope()))
                .collect(Collectors.toSet())
        );
    }
}
