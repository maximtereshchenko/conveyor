package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class WritePropertiesAction implements Runnable {

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
    public void run() {
        if (!Files.exists(destination.getParent())) {
            return;
        }
        var properties = properties();
        new FileTree(destination)
            .write(outputStream -> properties.store(outputStream, null));
        LOGGER.log(System.Logger.Level.INFO, "Wrote {0} to {1}", properties, destination);
    }

    private Properties properties() {
        var properties = new Properties();
        properties.put(Configuration.CLASS_PATH_DIRECTORY_KEY, classpathDirectory);
        properties.put(Configuration.LAUNCHED_CLASS_NAME_KEY, launchedClassName);
        return properties;
    }
}
