package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.StoredArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.StoredArtifactDefinitionReader;
import java.nio.file.Files;
import java.nio.file.Path;

final class DirectoryRepository {

    private final Path directory;
    private final StoredArtifactDefinitionReader reader;

    DirectoryRepository(Path directory, StoredArtifactDefinitionReader reader) {
        this.directory = directory;
        this.reader = reader;
    }

    Path artifact(ArtifactDefinition artifactDefinition) {
        var fullName = fullName(artifactDefinition);
        var artifact = directory.resolve(fullName + ".jar");
        if (!Files.exists(artifact)) {
            throw new IllegalArgumentException("Could not find artifact " + fullName);
        }
        return artifact;
    }

    StoredArtifactDefinition storedArtifactDefinition(ArtifactDefinition artifactDefinition) {
        var fullName = fullName(artifactDefinition);
        var definition = directory.resolve(fullName + ".json");
        if (!Files.exists(definition)) {
            throw new IllegalArgumentException("Could not find artifact definition " + fullName);
        }
        return reader.storedArtifactDefinition(definition);
    }

    private String fullName(ArtifactDefinition artifactDefinition) {
        return "%s-%d".formatted(artifactDefinition.name(), artifactDefinition.version());
    }
}
