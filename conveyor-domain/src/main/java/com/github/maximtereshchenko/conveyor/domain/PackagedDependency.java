package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

final class PackagedDependency implements Dependency {

    private final ArtifactDependencyDefinition artifactDependencyDefinition;

    PackagedDependency(ArtifactDependencyDefinition artifactDependencyDefinition) {
        this.artifactDependencyDefinition = artifactDependencyDefinition;
    }

    @Override
    public String name() {
        return artifactDependencyDefinition.name();
    }

    @Override
    public boolean in(ImmutableSet<DependencyScope> scopes) {
        return scopes.contains(artifactDependencyDefinition.scope());
    }

    @Override
    public Artifact artifact(Repositories repositories) {
        return new PackagedArtifact(
            artifactDependencyDefinition.name(),
            artifactDependencyDefinition.version(),
            repositories
        );
    }
}
