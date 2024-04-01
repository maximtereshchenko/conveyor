package com.github.maximtereshchenko.conveyor.domain;

final class DirectlyReferencedArtifact extends StoredArtifact {

    DirectlyReferencedArtifact(
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
        return artifactModel.version()
            .map(properties::interpolated)
            .map(SemanticVersion::new)
            .or(() -> preferences.version(artifactModel.group(), artifactModel.name()))
            .orElseThrow();
    }
}
