package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

final class WriteManifestTask extends BaseTask {

    private static final System.Logger LOGGER = System.getLogger(WriteManifestTask.class.getName());

    private final String mainClass;

    WriteManifestTask(ConveyorSchematic schematic, String mainClass) {
        super(schematic);
        this.mainClass = mainClass;
    }

    @Override
    public String name() {
        return "write-manifest";
    }

    @Override
    void onExplodedJar(ConveyorSchematic schematic, Path explodedJar) {
        try {
            var destination = Files.createDirectories(explodedJar.resolve("META-INF"))
                .resolve("MANIFEST.MF");
            try (var outputStream = Files.newOutputStream(destination)) {
                var manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.write(outputStream);
            }
            LOGGER.log(System.Logger.Level.INFO, "Wrote {0}", destination);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
