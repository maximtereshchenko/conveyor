package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;

final class ArchiveExecutableAction implements Runnable {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableAction.class.getName());

    private final Path containerDirectory;
    private final Path destination;

    ArchiveExecutableAction(Path containerDirectory, Path destination) {
        this.containerDirectory = containerDirectory;
        this.destination = destination;
    }

    @Override
    public void run() {
        if (!Files.exists(containerDirectory)) {
            return;
        }
        new ZipArchiveContainer(containerDirectory).archive(destination);
        LOGGER.log(
            System.Logger.Level.INFO,
            "Archived {0} to executable {1}",
            containerDirectory,
            destination
        );
    }
}
