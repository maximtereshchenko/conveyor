package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.springboot.Configuration;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class ExtractSpringBootLauncherAction implements Runnable {

    private static final System.Logger LOGGER =
        System.getLogger(ExtractSpringBootLauncherAction.class.getName());

    private final Path destination;

    ExtractSpringBootLauncherAction(Path destination) {
        this.destination = destination;
    }

    @Override
    public void run() {
        if (!Files.exists(destination)) {
            return;
        }
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
