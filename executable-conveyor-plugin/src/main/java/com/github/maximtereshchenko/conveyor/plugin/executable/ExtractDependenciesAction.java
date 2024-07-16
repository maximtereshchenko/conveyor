package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;

import java.nio.file.Path;
import java.util.Set;

final class ExtractDependenciesAction implements ConveyorTaskAction {

    private final Set<Path> dependencies;
    private final Path classesDirectory;

    ExtractDependenciesAction(Set<Path> dependencies, Path classesDirectory) {
        this.dependencies = dependencies;
        this.classesDirectory = classesDirectory;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        for (var dependency : dependencies) {
            new ZipArchive(dependency).extract(classesDirectory);
            tracer.submit(
                TracingImportance.INFO,
                () -> "Extracted %s to %s".formatted(dependency, classesDirectory)
            );
        }
        new FileTree(classesDirectory.resolve("module-info.class")).delete();
    }
}
