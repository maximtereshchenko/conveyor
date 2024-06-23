package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class ArchiveExecutableTask implements ConveyorTask {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableTask.class.getName());

    private final Path classesDirectory;
    private final Path destination;

    ArchiveExecutableTask(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public String name() {
        return "archive-executable";
    }

    @Override
    public Optional<Path> execute() {
        if (Files.exists(classesDirectory)) {
            archive();
        }
        return Optional.empty();
    }

    private void archive() {
        try {
            Files.createDirectories(destination.getParent());
            new ZipArchiveContainer(classesDirectory).archive(destination);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Archived {0} to executable {1}",
                classesDirectory,
                destination
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
