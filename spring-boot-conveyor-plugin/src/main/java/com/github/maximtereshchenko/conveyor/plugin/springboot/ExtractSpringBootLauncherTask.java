package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

final class ExtractSpringBootLauncherTask implements ConveyorTask {

    private static final System.Logger LOGGER =
        System.getLogger(ExtractSpringBootLauncherTask.class.getName());

    private final Path destination;

    ExtractSpringBootLauncherTask(Path destination) {
        this.destination = destination;
    }

    @Override
    public String name() {
        return "extract-spring-boot-launcher";
    }

    @Override
    public Optional<Path> execute() {
        if (Files.exists(destination)) {
            var path = path();
            new ZipArchive(path).extract(destination);
            LOGGER.log(System.Logger.Level.INFO, "Extracted {0} to {1}", path, destination);
        }
        return Optional.empty();
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
