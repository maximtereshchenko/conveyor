package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class WritePropertiesTask extends BaseTask {

    private static final System.Logger LOGGER =
        System.getLogger(WritePropertiesTask.class.getName());

    private final Path destination;
    private final String classPathDirectory;
    private final String launchedClassName;

    WritePropertiesTask(
        ConveyorSchematic schematic,
        Path destination,
        String classPathDirectory,
        String launchedClassName
    ) {
        super(schematic);
        this.destination = destination;
        this.classPathDirectory = classPathDirectory;
        this.launchedClassName = launchedClassName;
    }

    @Override
    public String name() {
        return "write-properties";
    }

    @Override
    void onExplodedJar(ConveyorSchematic schematic, Path explodedJar) {
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
        properties.put(Configuration.CLASS_PATH_DIRECTORY_KEY, classPathDirectory);
        properties.put(Configuration.LAUNCHED_CLASS_NAME_KEY, launchedClassName);
        return properties;
    }
}
