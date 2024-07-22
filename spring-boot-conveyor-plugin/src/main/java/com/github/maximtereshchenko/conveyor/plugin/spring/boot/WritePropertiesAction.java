package com.github.maximtereshchenko.conveyor.plugin.spring.boot;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import com.github.maximtereshchenko.conveyor.spring.boot.Configuration;

import java.nio.file.Path;
import java.util.Properties;

final class WritePropertiesAction implements ConveyorTaskAction {

    private final Path destination;
    private final String classpathDirectory;
    private final String launchedClassName;

    WritePropertiesAction(Path destination, String classpathDirectory, String launchedClassName) {
        this.destination = destination;
        this.classpathDirectory = classpathDirectory;
        this.launchedClassName = launchedClassName;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        var properties = properties();
        new FileTree(destination)
            .write(outputStream -> properties.store(outputStream, null));
        tracer.submit(
            TracingImportance.INFO,
            () -> "Wrote %s to %s".formatted(properties, destination)
        );
    }

    private Properties properties() {
        var properties = new Properties();
        properties.put(Configuration.CLASS_PATH_DIRECTORY_KEY, classpathDirectory);
        properties.put(Configuration.LAUNCHED_CLASS_NAME_KEY, launchedClassName);
        return properties;
    }
}
