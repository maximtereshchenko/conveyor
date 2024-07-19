package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

final class PublishExplodedJarArtifactTask implements ConveyorTaskAction {

    private final Path path;
    private final ConveyorSchematic schematic;

    PublishExplodedJarArtifactTask(Path path, ConveyorSchematic schematic) {
        this.path = path;
        this.schematic = schematic;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (!Files.exists(path)) {
            return;
        }
        schematic.publish(
            Convention.CONSTRUCTION_REPOSITORY_NAME,
            path,
            ArtifactClassifier.CLASSES
        );
        tracer.submit(
            TracingImportance.DEBUG,
            () -> "Published %s:%s to %s".formatted(
                path,
                ArtifactClassifier.CLASSES,
                Convention.CONSTRUCTION_REPOSITORY_NAME
            )
        );
    }
}
