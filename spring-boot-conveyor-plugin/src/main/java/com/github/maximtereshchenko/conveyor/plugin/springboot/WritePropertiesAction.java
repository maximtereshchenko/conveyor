package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

final class WritePropertiesAction implements Supplier<Optional<Path>> {

    private static final System.Logger LOGGER =
        System.getLogger(WritePropertiesAction.class.getName());

    private final Path destination;
    private final String classpathDirectory;
    private final String launchedClassName;

    WritePropertiesAction(Path destination, String classpathDirectory, String launchedClassName) {
        this.destination = destination;
        this.classpathDirectory = classpathDirectory;
        this.launchedClassName = launchedClassName;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(destination.getParent())) {
            write();
        }
        return Optional.empty();
    }

    private void write() {
        try (var outputStream = Files.newOutputStream(destination)) {
            var properties = properties();
            properties.store(outputStream, null);
            LOGGER.log(System.Logger.Level.INFO, "Wrote {0} to {1}", properties, destination);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Properties properties() {
        var properties = new Properties();
        properties.put(Configuration.CLASS_PATH_DIRECTORY_KEY, classpathDirectory);
        properties.put(Configuration.LAUNCHED_CLASS_NAME_KEY, launchedClassName);
        return properties;
    }
}
