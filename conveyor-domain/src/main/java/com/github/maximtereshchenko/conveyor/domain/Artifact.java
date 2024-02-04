package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Collection;

final class Artifact implements ArtifactDefinition {

    private final ArtifactDefinition artifactDefinition;
    private final DirectoryRepository repository;

    Artifact(ArtifactDefinition artifactDefinition, DirectoryRepository repository) {
        this.artifactDefinition = artifactDefinition;
        this.repository = repository;
    }

    @Override
    public String name() {
        return artifactDefinition.name();
    }

    @Override
    public int version() {
        return artifactDefinition.version();
    }

    Collection<Artifact> dependencies() {
        return repository.projectDefinition(artifactDefinition)
            .externalDependencies()
            .stream()
            .filter(dependencyDefinition -> dependencyDefinition.scope() != DependencyScope.TEST)
            .map(dependencyDefinition -> new Artifact(dependencyDefinition, repository))
            .toList();
    }

    Path modulePath() {
        return repository.artifact(artifactDefinition);
    }
}
