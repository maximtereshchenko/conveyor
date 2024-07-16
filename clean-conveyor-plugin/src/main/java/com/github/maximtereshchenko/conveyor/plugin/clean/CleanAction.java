package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.nio.file.Path;

final class CleanAction implements ConveyorTaskAction {

    private final Path path;

    CleanAction(Path path) {
        this.path = path;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        var fileTree = new FileTree(path);
        if (fileTree.exists()) {
            fileTree.delete();
            tracer.submit(TracingImportance.INFO, () -> "Removed " + path);
        } else {
            tracer.submit(TracingImportance.WARN, () -> path + " does not exist");
        }
    }
}
