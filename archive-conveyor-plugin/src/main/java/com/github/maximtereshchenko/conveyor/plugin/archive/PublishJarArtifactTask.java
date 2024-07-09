package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.Convention;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Files;
import java.nio.file.Path;

final class PublishJarArtifactTask implements Runnable {

    private final Path path;
    private final ConveyorSchematic schematic;

    PublishJarArtifactTask(Path path, ConveyorSchematic schematic) {
        this.path = path;
        this.schematic = schematic;
    }

    @Override
    public void run() {
        if (!Files.exists(path)) {
            return;
        }
        schematic.publish(
            Convention.CONSTRUCTION_REPOSITORY_NAME,
            path,
            ArtifactClassifier.JAR
        );
    }
}
