package com.github.maximtereshchenko.conveyor.domain;

final class TransitiveDependency extends ArtifactDependency {

    TransitiveDependency(
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
        return preferences.version(artifactDependencyModel.group(), artifactDependencyModel.name())
            .or(() -> artifactDependencyModel.version()
                .map(properties::interpolated)
                .map(SemanticVersion::new)
            )
            .orElseThrow();
    }
}
