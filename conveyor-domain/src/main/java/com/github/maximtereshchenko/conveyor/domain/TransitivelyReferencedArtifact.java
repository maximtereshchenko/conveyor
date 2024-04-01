package com.github.maximtereshchenko.conveyor.domain;

final class TransitivelyReferencedArtifact extends StoredArtifact {

    TransitivelyReferencedArtifact(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory
    ) {
        super(artifactModel, properties, preferences, repositories, schematicModelFactory);
    }

    @Override
    SemanticVersion semanticVersion(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences
    ) {
        return preferences.version(artifactModel.group(), artifactModel.name())
            .or(() ->
                artifactModel.version()
                    .map(properties::interpolated)
                    .map(SemanticVersion::new)
            )
            .orElseThrow();
    }
}
