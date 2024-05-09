package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class ArchiveExecutableTask extends BaseTask {

    private static final System.Logger LOGGER =
        System.getLogger(ArchiveExecutableTask.class.getName());

    private final Path container;

    ArchiveExecutableTask(ConveyorSchematic schematic, Path container) {
        super(schematic);
        this.container = container;
    }

    @Override
    public String name() {
        return "archive-executable";
    }

    @Override
    void onExplodedJar(ConveyorSchematic schematic, Path explodedJar) {
        try {
            var destination = destination(schematic);
            new ZipArchiveContainer(container).archive(destination);
            LOGGER.log(
                System.Logger.Level.INFO,
                "Archived {0} to executable {1}",
                container,
                destination
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path destination(ConveyorSchematic schematic) throws IOException {
        var coordinates = schematic.coordinates();
        return Files.createDirectories(schematic.constructionDirectory())
            .resolve("%s-%s-executable.jar".formatted(coordinates.name(), coordinates.version()));
    }
}
