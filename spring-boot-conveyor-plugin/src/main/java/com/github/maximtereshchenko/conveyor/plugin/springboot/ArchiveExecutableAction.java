package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;

final class ArchiveExecutableAction implements ConveyorTaskAction {

    private final Path containerDirectory;
    private final Path destination;

    ArchiveExecutableAction(Path containerDirectory, Path destination) {
        this.containerDirectory = containerDirectory;
        this.destination = destination;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (!Files.exists(containerDirectory)) {
            return;
        }
        new ZipArchiveContainer(containerDirectory).archive(destination);
        tracer.submit(
            TracingImportance.INFO,
            () -> "Archived %S to executable %s".formatted(containerDirectory, destination)
        );
    }
}
