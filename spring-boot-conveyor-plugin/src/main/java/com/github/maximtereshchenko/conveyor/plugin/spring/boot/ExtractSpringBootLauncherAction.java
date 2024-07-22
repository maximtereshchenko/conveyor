package com.github.maximtereshchenko.conveyor.plugin.spring.boot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import com.github.maximtereshchenko.conveyor.spring.boot.Configuration;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

final class ExtractSpringBootLauncherAction implements ConveyorTaskAction {

    private final Path destination;

    ExtractSpringBootLauncherAction(Path destination) {
        this.destination = destination;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        var path = path();
        new ZipArchive(path).extract(destination);
        tracer.submit(
            TracingImportance.INFO,
            () -> "Extracted %s to %s".formatted(path, destination)
        );
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
