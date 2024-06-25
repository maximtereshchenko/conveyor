package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

final class ArchiveExecutableAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableAction.class.getName());

    private final Path classesDirectory;
    private final Path destination;

    ArchiveExecutableAction(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public Optional<Path> get() {
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
