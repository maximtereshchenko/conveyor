package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import com.github.maximtereshchenko.conveyor.zip.ZipArchiveContainer;

import java.nio.file.Files;
import java.nio.file.Path;

final class ArchiveAction implements ConveyorTaskAction {

    private final Path classesDirectory;
    private final Path destination;

    ArchiveAction(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (Files.exists(classesDirectory)) {
            new ZipArchiveContainer(classesDirectory).archive(destination);
            tracer.submit(
                TracingImportance.INFO,
                () -> "Archived %s to %s".formatted(classesDirectory, destination)
            );
        } else {
            tracer.submit(TracingImportance.WARN, () -> "Nothing to archive");
        }
    }
}
