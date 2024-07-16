package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;

final class ArchiveExecutableAction implements ConveyorTaskAction {

    private final Path classesDirectory;
    private final Path destination;

    ArchiveExecutableAction(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (!Files.exists(classesDirectory)) {
            return;
        }
        new ZipArchiveContainer(classesDirectory).archive(destination);
        tracer.submit(
            TracingImportance.INFO,
            () -> "Archived %s to executable %s".formatted(classesDirectory, destination)
        );
    }
}
