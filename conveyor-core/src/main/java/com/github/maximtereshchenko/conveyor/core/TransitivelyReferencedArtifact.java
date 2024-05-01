package com.github.maximtereshchenko.conveyor.core;

import java.util.Set;

final class TransitivelyReferencedArtifact extends StoredArtifact {

    private final Preferences schematicPreferences;

    TransitivelyReferencedArtifact(
        ArtifactModel artifactModel,
        Preferences original,
        Preferences schematicPreferences,
        Properties properties,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory,
        PreferencesFactory preferencesFactory,
        Set<Id> propagatedExclusions
    ) {
        super(
            artifactModel,
            original,
            properties,
            repositories,
            schematicModelFactory,
            preferencesFactory,
            propagatedExclusions
        );
        this.schematicPreferences = schematicPreferences;
    }

    @Override
    Version version(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences
    ) {
        var id = artifactModel.idModel().id(properties);
        return preferences.version(id)
            .or(() ->
                artifactModel.version()
                    .map(properties::interpolated)
                    .map(Version::new)
                    .or(() -> schematicPreferences.version(id))
            )
            .orElseThrow();
    }
}
