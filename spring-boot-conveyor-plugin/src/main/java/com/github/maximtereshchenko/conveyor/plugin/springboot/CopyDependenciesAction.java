package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.nio.file.Path;
import java.util.Set;

final class CopyDependenciesAction implements ConveyorTaskAction {

    private final Set<Path> dependencies;
    private final Path destination;

    CopyDependenciesAction(Set<Path> dependencies, Path destination) {
        this.dependencies = dependencies;
        this.destination = destination;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        for (var dependency : dependencies) {
            var dependencyDestination = destination.resolve(dependency.getFileName());
            new FileTree(dependency).copyTo(dependencyDestination);
            tracer.submit(TracingImportance.INFO, () -> "Copied " + dependencyDestination);
        }
    }
}
