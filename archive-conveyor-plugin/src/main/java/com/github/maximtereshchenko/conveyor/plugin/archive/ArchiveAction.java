package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;

final class ArchiveAction implements Runnable {

    private static final System.Logger LOGGER = System.getLogger(ArchiveAction.class.getName());

    private final Path classesDirectory;
    private final Path destination;

    ArchiveAction(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public void run() {
        if (Files.exists(classesDirectory)) {
            archive();
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "Nothing to archive");
        }
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
