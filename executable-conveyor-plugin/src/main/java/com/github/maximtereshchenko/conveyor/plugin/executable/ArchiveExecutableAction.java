package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;

final class ArchiveExecutableAction implements Runnable {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableAction.class.getName());

    private final Path classesDirectory;
    private final Path destination;

    ArchiveExecutableAction(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public void run() {
        if (!Files.exists(classesDirectory)) {
            return;
        }
        new ZipArchiveContainer(classesDirectory).archive(destination);
        LOGGER.log(
            System.Logger.Level.INFO,
            "Archived {0} to executable {1}",
            classesDirectory,
            destination
        );
    }
}
