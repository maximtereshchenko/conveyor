package com.github.maximtereshchenko.conveyor.domain;

final class DirectDependency extends ArtifactDependency {

    DirectDependency(
        ArtifactDependencyModel artifactDependencyModel,
        ModelFactory modelFactory,
        Properties properties,
        Preferences preferences,
        Repositories repositories
    ) {
        super(artifactDependencyModel, modelFactory, properties, preferences, repositories);
    }

    @Override
    SemanticVersion version(
        ArtifactDependencyModel artifactDependencyModel,
        Properties properties,
        Preferences preferences
    ) {
        return artifactDependencyModel.version()
            .map(properties::interpolated)
            .map(SemanticVersion::new)
            .or(() -> preferences.version(artifactDependencyModel.name()))
            .orElseThrow();
    }
}
