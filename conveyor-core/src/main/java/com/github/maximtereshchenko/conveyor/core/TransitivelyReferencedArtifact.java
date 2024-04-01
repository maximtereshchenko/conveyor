package com.github.maximtereshchenko.conveyor.core;

final class TransitivelyReferencedArtifact extends StoredArtifact {

    TransitivelyReferencedArtifact(
        ArtifactModel artifactModel,
        Preferences preferences,
        Properties properties,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory
    ) {
        super(artifactModel, preferences, properties, repositories, schematicModelFactory);
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
            )
            .orElseThrow();
    }
}
