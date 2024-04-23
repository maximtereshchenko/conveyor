package com.github.maximtereshchenko.conveyor.core;

final class TransitivelyReferencedArtifact extends StoredArtifact {

    private final Preferences schematicPreferences;

    TransitivelyReferencedArtifact(
        ArtifactModel artifactModel,
        Preferences original,
        Preferences schematicPreferences,
        Properties properties,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory,
        PreferencesFactory preferencesFactory
    ) {
        super(
            artifactModel,
            original,
            properties,
            repositories,
            schematicModelFactory,
            preferencesFactory
        );
        this.schematicPreferences = schematicPreferences;
    }

    @Override
    SemanticVersion semanticVersion(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences
    ) {
        return preferences.version(artifactModel.id())
            .or(() ->
                artifactModel.version()
                    .map(properties::interpolated)
                    .map(SemanticVersion::new)
                    .or(() -> schematicPreferences.version(artifactModel.id()))
            )
            .orElseThrow();
    }
}
