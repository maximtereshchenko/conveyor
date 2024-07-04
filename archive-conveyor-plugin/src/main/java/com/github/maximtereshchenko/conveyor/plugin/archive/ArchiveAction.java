package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

final class ArchiveAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER = System.getLogger(ArchiveAction.class.getName());

    private final Path classesDirectory;
    private final Path destination;

    ArchiveAction(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(classesDirectory)) {
            archive();
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "Nothing to archive");
        }
        return Optional.empty();
    }

    private void archive() {
        new ZipArchiveContainer(classesDirectory).archive(destination);
        LOGGER.log(
            System.Logger.Level.INFO,
            "Archived {0} to {1}",
            classesDirectory,
            destination
        );
    }
}
