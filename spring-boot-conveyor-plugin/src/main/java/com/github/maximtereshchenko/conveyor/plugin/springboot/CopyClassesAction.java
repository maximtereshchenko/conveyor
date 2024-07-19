package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.nio.file.Path;

final class CopyClassesAction implements ConveyorTaskAction {

    private final Path classesDirectory;
    private final Path destination;

    CopyClassesAction(Path classesDirectory, Path destination) {
        this.classesDirectory = classesDirectory;
        this.destination = destination;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        var fileTree = new FileTree(classesDirectory);
        if (!fileTree.exists()) {
            return;
        }
        fileTree.copyTo(destination);
        tracer.submit(
            TracingImportance.INFO,
            () -> "Copied %s to %s".formatted(classesDirectory, destination)
        );
    }
}
