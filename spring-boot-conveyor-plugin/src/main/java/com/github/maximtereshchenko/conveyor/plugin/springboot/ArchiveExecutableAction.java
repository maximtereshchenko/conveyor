package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

final class ArchiveExecutableAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableAction.class.getName());

    private final Path containerDirectory;
    private final Path destination;

    ArchiveExecutableAction(Path containerDirectory, Path destination) {
        this.containerDirectory = containerDirectory;
        this.destination = destination;
    }

    @Override
    public Optional<Path> get() {
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
