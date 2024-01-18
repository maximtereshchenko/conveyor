package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import java.nio.file.Files;
import java.nio.file.Path;

final class DirectoryRepository {

    private final Path directory;
    private final ProjectDefinitionReader reader;

    DirectoryRepository(Path directory, ProjectDefinitionReader reader) {
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

    ProjectDefinition projectDefinition(ArtifactDefinition artifactDefinition) {
        var fullName = fullName(artifactDefinition);
        var definition = directory.resolve(fullName + ".json");
        if (!Files.exists(definition)) {
            throw new IllegalArgumentException("Could not find artifact definition " + fullName);
        }
        return reader.projectDefinition(definition);
    }

    private String fullName(ArtifactDefinition artifactDefinition) {
        return "%s-%d".formatted(artifactDefinition.name(), artifactDefinition.version());
    }
}
