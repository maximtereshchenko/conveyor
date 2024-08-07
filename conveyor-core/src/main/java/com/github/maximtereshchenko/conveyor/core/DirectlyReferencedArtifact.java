package com.github.maximtereshchenko.conveyor.core;

import java.util.Set;

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
            preferencesFactory,
            Set.of()
        );
    }

    @Override
    Version version(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences
    ) {
        return artifactModel.version()
            .map(properties::interpolated)
            .map(Version::new)
            .or(() -> preferences.version(artifactModel.idModel().id(properties)))
            .orElseThrow();
    }
}
