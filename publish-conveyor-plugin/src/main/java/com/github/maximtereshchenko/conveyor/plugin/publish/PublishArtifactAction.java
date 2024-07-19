package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

final class PublishArtifactAction implements ConveyorTaskAction {

    private final Path path;
    private final ConveyorSchematic schematic;
    private final String repository;
    private final ArtifactClassifier artifactClassifier;

    PublishArtifactAction(
        Path path,
        ConveyorSchematic schematic,
        String repository,
        ArtifactClassifier artifactClassifier
    ) {
        this.path = path;
        this.schematic = schematic;
        this.repository = repository;
        this.artifactClassifier = artifactClassifier;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (!Files.exists(path)) {
            return;
        }
        schematic.publish(repository, path, artifactClassifier);
        tracer.submit(
            TracingImportance.INFO,
            () -> "Published %s:%s to %s".formatted(path, artifactClassifier, repository)
        );
    }
}
