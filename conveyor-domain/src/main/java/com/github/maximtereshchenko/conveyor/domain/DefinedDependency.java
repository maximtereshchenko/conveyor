package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.List;

final class DefinedDependency implements Dependency {

    private final Artifact artifact;
    private final ArtifactDependencyDefinition artifactDependencyDefinition;

    DefinedDependency(ArtifactDependencyDefinition artifactDependencyDefinition, Repository repository) {
        this.artifact = new DefinedArtifact(artifactDependencyDefinition, repository);
        this.artifactDependencyDefinition = artifactDependencyDefinition;
    }

    @Override
    public String name() {
        return artifact.name();
    }

    @Override
    public int version() {
        return artifact.version();
    }

    @Override
    public Dependencies dependencies() {
        return artifact.dependencies();
    }

    @Override
    public Path modulePath() {
        return artifact.modulePath();
    }

    @Override
    public boolean hasAny(DependencyScope... scopes) {
        return List.of(scopes).contains(artifactDependencyDefinition.scope());
    }
}
