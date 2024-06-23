package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class CleanTask implements ConveyorTask {

    private static final System.Logger LOGGER = System.getLogger(CleanTask.class.getName());

    private final Path path;

    CleanTask(Path path) {
        this.path = path;
    }

    @Override
    public String name() {
        return "clean";
    }

    @Override
    public Optional<Path> execute() {
        if (Files.exists(path)) {
            deleteRecursively(path);
            LOGGER.log(System.Logger.Level.INFO, "Removed {0}", path);
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "{0} does not exist", path);
        }
        return Optional.empty();
    }

    private void deleteRecursively(Path path) {
        try {
            Files.walkFileTree(path, new DeleteRecursivelyFileVisitor());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
