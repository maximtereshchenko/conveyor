package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

final class ArchiveExecutableTask implements ConveyorTask {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableTask.class.getName());

    private final Path containerDirectory;
    private final Path destination;

    ArchiveExecutableTask(Path containerDirectory, Path destination) {
        this.containerDirectory = containerDirectory;
        this.destination = destination;
    }

    @Override
    public String name() {
        return "archive-executable";
    }

    @Override
    public Optional<Path> execute() {
        if (Files.exists(containerDirectory)) {
            archive();
        }
        return Optional.empty();
    }

    private void archive() {
        new ZipArchiveContainer(containerDirectory).archive(destination);
        LOGGER.log(
            System.Logger.Level.INFO,
            "Archived {0} to executable {1}",
            containerDirectory,
            destination
        );
    }
}
