package com.github.maximtereshchenko.conveyor.domain;

final class TransitiveDependency extends ArtifactDependency {

    TransitiveDependency(
        ArtifactDependencyModel artifactDependencyModel,
        ModelFactory modelFactory,
        Preferences preferences,
        Repositories repositories
    ) {
        super(artifactDependencyModel, modelFactory, preferences, repositories);
    }

    @Override
    int version(ArtifactDependencyModel artifactDependencyModel, Preferences preferences) {
        return preferences.version(artifactDependencyModel.name())
            .or(artifactDependencyModel::version)
            .orElseThrow();
    }
}
