package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

final class PublishArtifactAction implements ConveyorTaskAction {

    private final Path artifact;
    private final String repository;
    private final ConveyorSchematic schematic;

    PublishArtifactAction(Path artifact, String repository, ConveyorSchematic schematic) {
        this.artifact = artifact;
        this.repository = repository;
        this.schematic = schematic;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        publish(artifact, ArtifactClassifier.CLASSES, tracer);
        publish(schematic.path(), ArtifactClassifier.SCHEMATIC_DEFINITION, tracer);
    }

    private void publish(
        Path path,
        ArtifactClassifier artifactClassifier,
        ConveyorTaskTracer tracer
    ) {
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
