package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Files;
import java.nio.file.Path;

final class PublishArtifactAction implements Runnable {

    private static final System.Logger LOGGER =
        System.getLogger(PublishArtifactAction.class.getName());

    private final Path artifact;
    private final String repository;
    private final ConveyorSchematic schematic;

    PublishArtifactAction(Path artifact, String repository, ConveyorSchematic schematic) {
        this.artifact = artifact;
        this.repository = repository;
        this.schematic = schematic;
    }

    @Override
    public void run() {
        publish(artifact, ArtifactClassifier.CLASSES);
        publish(schematic.path(), ArtifactClassifier.SCHEMATIC_DEFINITION);
    }

    private void publish(Path path, ArtifactClassifier artifactClassifier) {
        if (!Files.exists(path)) {
            return;
        }
        schematic.publish(repository, path, artifactClassifier);
        LOGGER.log(
            System.Logger.Level.INFO,
            "Published {0}:{1} to {2}",
            path,
            artifactClassifier,
            repository
        );
    }
}
