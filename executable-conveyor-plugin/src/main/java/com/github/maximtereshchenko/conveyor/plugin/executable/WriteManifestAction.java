package com.github.maximtereshchenko.conveyor.plugin.executable;

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

    private final Path classesDirectory;
    private final String mainClass;

    WriteManifestAction(Path classesDirectory, String mainClass) {
        this.classesDirectory = classesDirectory;
        this.mainClass = mainClass;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(classesDirectory)) {
            write();
        }
        return Optional.empty();
    }

    private void write() {
        try {
            var destination = Files.createDirectories(classesDirectory.resolve("META-INF"))
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