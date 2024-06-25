package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

final class WriteManifestAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(WriteManifestAction.class.getName());

    private final Path containerDirectory;

    WriteManifestAction(Path containerDirectory) {
        this.containerDirectory = containerDirectory;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(containerDirectory)) {
            write();
        }
        return Optional.empty();
    }

    private void write() {
        try {
            var manifestLocation = Files.createDirectories(containerDirectory.resolve("META-INF"))
                .resolve("MANIFEST.MF");
            try (var outputStream = Files.newOutputStream(manifestLocation)) {
                var manifest = new Manifest();
                var mainAttributes = manifest.getMainAttributes();
                mainAttributes.put(Attributes.Name.MAIN_CLASS, Configuration.MAIN_CLASS_NAME);
                mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.write(outputStream);
            }
            LOGGER.log(System.Logger.Level.INFO, "Wrote {0}", manifestLocation);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
