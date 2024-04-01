package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Set;

final class Dependency implements Artifact {

    private final Artifact artifact;
    private final DependencyModel dependencyModel;

    Dependency(Artifact artifact, DependencyModel dependencyModel) {
        this.artifact = artifact;
        this.dependencyModel = dependencyModel;
    }

    @Override
    public String group() {
        return artifact.group();
    }

    @Override
    public String name() {
        return artifact.name();
    }

    @Override
    public SemanticVersion semanticVersion() {
        return artifact.semanticVersion();
    }

    @Override
    public Path path() {
        return artifact.path();
    }

    @Override
    public Set<Artifact> dependencies() {
        return artifact.dependencies();
    }

    DependencyScope scope() {
        return dependencyModel.scope().orElse(DependencyScope.IMPLEMENTATION);
    }
}
