package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.Convention;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Files;
import java.nio.file.Path;

final class PublishExplodedJarArtifactTask implements Runnable {

    private final Path path;
    private final ConveyorSchematic schematic;

    PublishExplodedJarArtifactTask(Path path, ConveyorSchematic schematic) {
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
            ArtifactClassifier.CLASSES
        );
    }
}
