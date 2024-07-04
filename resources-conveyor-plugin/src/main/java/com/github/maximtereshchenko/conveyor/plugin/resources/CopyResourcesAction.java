package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.filevisitors.Copy;

import java.io.IOException;
import java.io.UncheckedIOException;
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
            copy();
        }
        return Optional.empty();
    }

    private void copy() {
        try {
            Files.walkFileTree(resourcesDirectory, new Copy(resourcesDirectory, classesDirectory));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
