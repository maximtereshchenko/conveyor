package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

final class WriteManifestAction implements ConveyorTaskAction {

    private final Path classesDirectory;
    private final String mainClass;

    WriteManifestAction(Path classesDirectory, String mainClass) {
        this.classesDirectory = classesDirectory;
        this.mainClass = mainClass;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (!Files.exists(classesDirectory)) {
            return;
        }
        var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        var destination = classesDirectory.resolve("META-INF").resolve("MANIFEST.MF");
        new FileTree(destination).write(manifest::write);
        tracer.submit(TracingImportance.INFO, () -> "Wrote " + destination);
    }
}
