package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class PublishArtifactTask implements ConveyorTask {

    private static final System.Logger LOGGER =
        System.getLogger(PublishArtifactTask.class.getName());

    private final Path artifact;
    private final String repository;
    private final ConveyorSchematic schematic;

    PublishArtifactTask(Path artifact, String repository, ConveyorSchematic schematic) {
        this.artifact = artifact;
        this.repository = repository;
        this.schematic = schematic;
    }

    @Override
    public String name() {
        return "publish-artifact";
    }

    @Override
    public Optional<Path> execute() {
        publish(artifact, ArtifactClassifier.JAR);
        publish(schematic.path(), ArtifactClassifier.SCHEMATIC_DEFINITION);
        return Optional.empty();
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
            ArtifactClassifier.JAR,
            repository
        );
    }
}
