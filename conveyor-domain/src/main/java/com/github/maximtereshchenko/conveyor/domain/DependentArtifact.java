package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Set;
import java.util.stream.Collectors;

abstract class DependentArtifact<T extends DependencyModel> implements Artifact {

    @Override
    public Set<Artifact> dependencies() {
        return dependencyModels()
            .stream()
            .map(this::dependency)
            .filter(dependency -> dependency.scope() != DependencyScope.TEST)
            .collect(Collectors.toSet());
    }

    abstract Set<T> dependencyModels();

    abstract Dependency dependency(T dependencyModel);
}
