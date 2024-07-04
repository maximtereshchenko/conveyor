package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

final class CopyResourcesAction implements Supplier<Optional<Path>> {

    private final Path resourcesDirectory;
    private final Path classesDirectory;

    CopyResourcesAction(Path resourcesDirectory, Path classesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
        this.classesDirectory = classesDirectory;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(resourcesDirectory) && Files.exists(classesDirectory)) {
            new FileTree(resourcesDirectory).copyTo(classesDirectory);
        }
        return Optional.empty();
    }
}
