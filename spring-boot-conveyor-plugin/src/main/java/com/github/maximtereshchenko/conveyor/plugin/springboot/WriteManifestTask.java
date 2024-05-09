package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

final class WriteManifestTask extends BaseTask {

    private static final System.Logger LOGGER = System.getLogger(WriteManifestTask.class.getName());

    private final Path destination;

    WriteManifestTask(ConveyorSchematic schematic, Path destination) {
        super(schematic);
        this.destination = destination;
    }

    @Override
    public String name() {
        return "write-manifest";
    }

    @Override
    void onExplodedJar(ConveyorSchematic schematic, Path explodedJar) {
        try {
            var manifestPath = Files.createDirectories(destination.resolve("META-INF"))
                .resolve("MANIFEST.MF");
            try (var outputStream = Files.newOutputStream(manifestPath)) {
                var manifest = new Manifest();
                var mainAttributes = manifest.getMainAttributes();
                mainAttributes.put(Attributes.Name.MAIN_CLASS, Configuration.MAIN_CLASS_NAME);
                mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.write(outputStream);
            }
            LOGGER.log(System.Logger.Level.INFO, "Wrote {0}", manifestPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
