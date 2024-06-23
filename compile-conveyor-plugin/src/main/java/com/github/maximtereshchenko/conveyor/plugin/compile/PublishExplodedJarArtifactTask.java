package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class PublishExplodedJarArtifactTask implements ConveyorTask {

    private final Path path;

    PublishExplodedJarArtifactTask(Path path) {
        this.path = path;
    }

    @Override
    public String name() {
        return "publish-exploded-jar-artifact";
    }

    @Override
    public Optional<Path> execute() {
        if (Files.exists(path)) {
            return Optional.of(path);
        }
        return Optional.empty();
    }
}
