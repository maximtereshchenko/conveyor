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

    Path artifact(VersionedArtifactDefinition versionedArtifactDefinition) {
        var fullName = "%s-%d".formatted(versionedArtifactDefinition.name(), versionedArtifactDefinition.version());
        var artifact = directory.resolve(fullName + ".jar");
        if (!Files.exists(artifact)) {
            throw new IllegalArgumentException("Could not find artifact " + fullName);
        }
        return artifact;
    }

    ArtifactDefinition artifactDefinition(VersionedArtifactDefinition versionedArtifactDefinition) {
        return artifactDefinition(versionedArtifactDefinition.name(), versionedArtifactDefinition.version());
    }

    ArtifactDefinition artifactDefinition(String name, int version) {
        var fullName = "%s-%d".formatted(name, version);
        var definition = directory.resolve(fullName + ".json");
        if (!Files.exists(definition)) {
            throw new IllegalArgumentException("Could not find artifact definition " + fullName);
        }
        return jsonReader.read(definition, ArtifactDefinition.class);
    }
}
