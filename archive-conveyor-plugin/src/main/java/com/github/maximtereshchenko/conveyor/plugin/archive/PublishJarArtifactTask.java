package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

final class PublishJarArtifactTask implements ConveyorTaskAction {

    private final Path path;
    private final ConveyorSchematic schematic;

    PublishJarArtifactTask(Path path, ConveyorSchematic schematic) {
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
    }
}
