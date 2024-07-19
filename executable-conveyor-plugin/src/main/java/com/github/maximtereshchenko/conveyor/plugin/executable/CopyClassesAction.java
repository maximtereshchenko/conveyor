package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.nio.file.Path;

final class CopyClassesAction implements ConveyorTaskAction {

    private final Path classesDirectory;
    private final Path containerDirectory;

    CopyClassesAction(Path classesDirectory, Path containerDirectory) {
        this.classesDirectory = classesDirectory;
        this.containerDirectory = containerDirectory;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        var fileTree = new FileTree(classesDirectory);
        if (!fileTree.exists()) {
            return;
        }
        fileTree.copyTo(containerDirectory);
        tracer.submit(
            TracingImportance.INFO,
            () -> "Copied %s to %s".formatted(classesDirectory, containerDirectory)
        );
    }
}
