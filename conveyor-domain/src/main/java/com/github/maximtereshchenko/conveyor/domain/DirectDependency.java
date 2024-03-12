package com.github.maximtereshchenko.conveyor.domain;

final class DirectDependency extends ArtifactDependency {

    DirectDependency(
        ArtifactDependencyModel artifactDependencyModel,
        ModelFactory modelFactory,
        Preferences preferences,
        Repositories repositories
    ) {
        super(artifactDependencyModel, modelFactory, preferences, repositories);
    }

    @Override
    int version(ArtifactDependencyModel artifactDependencyModel, Preferences preferences) {
        return artifactDependencyModel.version()
            .or(() -> preferences.version(artifactDependencyModel.name()))
            .orElseThrow();
    }
}
