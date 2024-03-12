package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Set;

abstract class ArtifactDependency extends StoredArtifact<ArtifactDependencyModel> implements Dependency {

    private final ArtifactDependencyModel artifactDependencyModel;
    private final ModelFactory modelFactory;
    private final Preferences preferences;

    ArtifactDependency(
        ArtifactDependencyModel artifactDependencyModel,
        ModelFactory modelFactory,
        Preferences preferences,
        Repositories repositories
    ) {
        super(repositories);
        this.artifactDependencyModel = artifactDependencyModel;
        this.modelFactory = modelFactory;
        this.preferences = preferences;
    }

    @Override
    public String name() {
        return artifactDependencyModel.name();
    }

    @Override
    public int version() {
        return version(artifactDependencyModel, preferences);
    }

    @Override
    public DependencyScope scope() {
        return artifactDependencyModel.scope().orElse(DependencyScope.IMPLEMENTATION);
    }

    @Override
    Set<ArtifactDependencyModel> dependencyModels() {
        return modelFactory.manualHierarchy(artifactDependencyModel.name(), version(), repositories()).dependencies();
    }

    @Override
    Dependency dependency(ArtifactDependencyModel dependencyModel) {
        return new TransitiveDependency(dependencyModel, modelFactory, preferences, repositories());
    }

    abstract int version(ArtifactDependencyModel artifactDependencyModel, Preferences preferences);
}
