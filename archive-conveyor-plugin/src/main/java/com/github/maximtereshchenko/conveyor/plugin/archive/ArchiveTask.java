package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class ArchiveTask implements ConveyorTask {

    private static final System.Logger LOGGER = System.getLogger(ArchiveTask.class.getName());

    private final Path classesDirectory;
    private final Path destination;

    ArchiveTask(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public String name() {
        return "archive";
    }

    @Override
    public Optional<Path> execute() {
        if (!Files.exists(classesDirectory)) {
            LOGGER.log(System.Logger.Level.WARNING, "Nothing to archive");
            return Optional.empty();
        }
        archive();
        return Optional.of(destination);
    }

    private void archive() {
        try {
            Files.createDirectories(destination.getParent());
            new ZipArchiveContainer(classesDirectory).archive(destination);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Archived {0} to {1}",
                classesDirectory,
                destination
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
