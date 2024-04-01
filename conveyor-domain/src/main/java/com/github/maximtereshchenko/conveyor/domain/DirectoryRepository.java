package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.JsonReader;
import java.nio.file.Files;
import java.nio.file.Path;

final class DirectoryRepository {

    private final Path directory;
    private final JsonReader jsonReader;

    DirectoryRepository(Path directory, JsonReader jsonReader) {
        this.directory = directory;
        this.jsonReader = jsonReader;
    }

    Path artifact(ArtifactDefinition artifactDefinition) {
        var fullName = fullName(artifactDefinition);
        var artifact = directory.resolve(fullName + ".jar");
        if (!Files.exists(artifact)) {
            throw new IllegalArgumentException("Could not find artifact " + fullName);
        }
        return artifact;
    }

    StoredArtifactDefinition artifactDefinition(ArtifactDefinition artifactDefinition) {
        var fullName = fullName(artifactDefinition);
        var definition = directory.resolve(fullName + ".json");
        if (!Files.exists(definition)) {
            throw new IllegalArgumentException("Could not find artifact definition " + fullName);
        }
        return jsonReader.read(definition, StoredArtifactDefinition.class);
    }

    private String fullName(ArtifactDefinition artifactDefinition) {
        return "%s-%d".formatted(artifactDefinition.name(), artifactDefinition.version());
    }
}
