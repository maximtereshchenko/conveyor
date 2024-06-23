package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class CopyResourcesTask implements ConveyorTask {

    private final String name;
    private final Path resourcesDirectory;
    private final Path classesDirectory;

    CopyResourcesTask(String name, Path resourcesDirectory, Path classesDirectory) {
        this.name = name;
        this.resourcesDirectory = resourcesDirectory;
        this.classesDirectory = classesDirectory;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<Path> execute() {
        if (Files.exists(resourcesDirectory) && Files.exists(classesDirectory)) {
            copy();
        }
        return Optional.empty();
    }

    private void copy() {
        try {
            Files.walkFileTree(
                resourcesDirectory,
                new CopyRecursively(resourcesDirectory, classesDirectory)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
