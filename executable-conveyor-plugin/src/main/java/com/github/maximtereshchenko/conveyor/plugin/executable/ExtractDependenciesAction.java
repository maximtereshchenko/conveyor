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
    private final Path containerDirectory;

    ExtractDependenciesAction(Set<Path> dependencies, Path containerDirectory) {
        this.dependencies = dependencies;
        this.containerDirectory = containerDirectory;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        for (var dependency : dependencies) {
            new ZipArchive(dependency).extract(containerDirectory);
            tracer.submit(
                TracingImportance.INFO,
                () -> "Extracted %s to %s".formatted(dependency, containerDirectory)
            );
        }
        new FileTree(containerDirectory.resolve("module-info.class")).delete();
    }
}
