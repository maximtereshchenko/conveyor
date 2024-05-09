package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

final class ExtractSpringBootLauncherTask extends BaseTask {

    private static final System.Logger LOGGER =
        System.getLogger(ExtractSpringBootLauncherTask.class.getName());

    private final Path destination;

    ExtractSpringBootLauncherTask(ConveyorSchematic schematic, Path destination) {
        super(schematic);
        this.destination = destination;
    }

    @Override
    public String name() {
        return "extract-spring-boot-launcher";
    }

    @Override
    void onExplodedJar(ConveyorSchematic schematic, Path explodedJar) {
        var path = path();
        new ZipArchive(path).extract(destination);
        LOGGER.log(System.Logger.Level.INFO, "Extracted {0} to {1}", path, destination);
    }

    private Path path() {
        try {
            return Paths.get(
                Configuration.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
