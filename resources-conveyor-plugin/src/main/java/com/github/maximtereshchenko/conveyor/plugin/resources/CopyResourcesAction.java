package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;

import java.nio.file.Path;

final class CopyResourcesAction implements ConveyorTaskAction {

    private final Path resourcesDirectory;
    private final Path destinationDirectory;

    CopyResourcesAction(Path resourcesDirectory, Path destinationDirectory) {
        this.resourcesDirectory = resourcesDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        var fileTree = new FileTree(resourcesDirectory);
        if (fileTree.exists()) {
            fileTree.copyTo(destinationDirectory);
        }
    }
}
