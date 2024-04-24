package com.github.maximtereshchenko.conveyor.core;

final class DirectlyReferencedArtifact extends StoredArtifact {

    DirectlyReferencedArtifact(
        ArtifactModel artifactModel,
        Preferences preferences,
        Properties properties,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory,
        PreferencesFactory preferencesFactory
    ) {
        super(
            artifactModel,
            preferences,
            properties,
            repositories,
            schematicModelFactory,
            preferencesFactory
        );
    }

    @Override
    SemanticVersion semanticVersion(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences
    ) {
        return artifactModel.version()
            .map(properties::interpolated)
            .map(SemanticVersion::new)
            .or(() -> preferences.version(artifactModel.idModel().id(properties)))
            .orElseThrow();
    }
}
