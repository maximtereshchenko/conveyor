package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

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
                Stage.ARCHIVE,
                Step.RUN,
                new ArchiveAction(classesDirectory, destination),
                Set.of(classesDirectory),
                Set.of(destination),
                Cache.ENABLED
            ),
            new ConveyorTask(
                "publish-jar-artifact",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new PublishJarArtifactTask(destination),
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
