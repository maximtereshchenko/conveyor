package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.zip.ZipArchiveContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class ArchiveExecutableTask extends BaseTask {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableTask.class.getName());

    private final Path destination;

    ArchiveExecutableTask(ConveyorSchematic schematic, Path destination) {
        super(schematic);
        this.destination = destination;
    }

    @Override
    public String name() {
        return "archive-executable";
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        explodedJar(products).ifPresent(this::archiveExecutable);
        return Set.of();
    }

    private void archiveExecutable(Path explodedJar) {
        try {
            Files.createDirectories(destination.getParent());
            new ZipArchiveContainer(explodedJar).archive(destination);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Archived {0} to executable {1}",
                explodedJar,
                destination
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
