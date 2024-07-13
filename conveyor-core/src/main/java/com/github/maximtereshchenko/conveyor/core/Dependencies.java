package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.DependencyScope;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class Dependencies {

    private final Set<Dependency> all;
    private final ClasspathFactory classpathFactory;

    Dependencies(Set<Dependency> all, ClasspathFactory classpathFactory) {
        this.all = all;
        this.classpathFactory = classpathFactory;
    }

    Set<Path> classpath(Set<DependencyScope> scopes) {
        return classpathFactory.classpath(
            all.stream()
                .filter(dependency -> scopes.contains(dependency.scope()))
                .collect(Collectors.toSet())
        );
    }
}
