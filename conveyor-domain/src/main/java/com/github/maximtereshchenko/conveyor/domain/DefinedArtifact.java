package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;

final class DefinedArtifact implements Artifact {

    private final ArtifactDefinition artifactDefinition;
    private final Repository repository;

    DefinedArtifact(ArtifactDefinition artifactDefinition, Repository repository) {
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

    @Override
    public Dependencies dependencies() {
        return Dependencies.from(
            repository.manualDefinition(name(), version())
                .dependencies()
                .stream()
                .map(dependencyDefinition -> new DefinedDependency(dependencyDefinition, repository))
                .filter(dependency -> dependency.hasAny(DependencyScope.IMPLEMENTATION))
                .collect(new ImmutableSetCollector<>())
        );
    }

    @Override
    public Path modulePath() {
        return repository.path(name(), version());
    }
}
