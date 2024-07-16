package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskAction;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskTracer;
import com.github.maximtereshchenko.conveyor.plugin.api.TracingImportance;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

final class WriteManifestAction implements ConveyorTaskAction {

    private final Path containerDirectory;

    WriteManifestAction(Path containerDirectory) {
        this.containerDirectory = containerDirectory;
    }

    @Override
    public void execute(ConveyorTaskTracer tracer) {
        if (!Files.exists(containerDirectory)) {
            return;
        }
        var manifest = new Manifest();
        var mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MAIN_CLASS, Configuration.MAIN_CLASS_NAME);
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        var manifestLocation = containerDirectory.resolve("META-INF").resolve("MANIFEST.MF");
        new FileTree(manifestLocation).write(manifest::write);
        tracer.submit(TracingImportance.INFO, () -> "Wrote " + manifestLocation);
    }
}
