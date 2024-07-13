package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ArchivePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "archive-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var classesDirectory = configuredPath(configuration, "classes.directory");
        var destination = configuredPath(configuration, "destination");
        return List.of(
            new ConveyorTask(
                "archive",
                BindingStage.ARCHIVE,
                BindingStep.RUN,
                new ArchiveAction(classesDirectory, destination),
                Set.of(new PathConveyorTaskInput(classesDirectory)),
                Set.of(new PathConveyorTaskOutput(destination)),
                Cache.ENABLED
            ),
            new ConveyorTask(
                "publish-jar-artifact",
                BindingStage.ARCHIVE,
                BindingStep.FINALIZE,
                new PublishJarArtifactTask(destination, schematic),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
