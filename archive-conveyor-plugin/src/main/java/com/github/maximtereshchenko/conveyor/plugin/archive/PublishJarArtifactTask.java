package com.github.maximtereshchenko.conveyor.plugin.archive;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

final class PublishJarArtifactTask implements Supplier<Optional<Path>> {

    private final Path path;

    PublishJarArtifactTask(Path path) {
        this.path = path;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(path)) {
            return Optional.of(path);
        }
        return Optional.empty();
    }
}
