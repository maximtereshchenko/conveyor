package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class CopyClasspathAction implements ConveyorTaskAction {

    private final Path classesDirectory;
    private final Set<Path> dependencies;
    private final Path destination;

    CopyClasspathAction(Path classesDirectory, Set<Path> dependencies, Path destination) {
        this.classesDirectory = classesDirectory;
        this.dependencies = dependencies;
        this.destination = destination;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (!Files.exists(classesDirectory)) {
            return;
        }
        copyClasses(tracer);
        copyDependencies(tracer);
    }

    private void copyClasses(ConveyorTaskTracer tracer) {
        var classesDestination = destination.resolve("classes");
        new FileTree(classesDirectory).copyTo(classesDestination);
        tracer.submit(
            TracingImportance.INFO,
            () -> "Copied %s to %s".formatted(classesDirectory, classesDestination)
        );
    }

    private void copyDependencies(ConveyorTaskTracer tracer) {
        for (var dependency : dependencies) {
            var dependencyDestination = destination.resolve(dependency.getFileName());
            new FileTree(dependency).copyTo(dependencyDestination);
            tracer.submit(TracingImportance.INFO, () -> "Copied " + dependencyDestination);
        }
    }
}
