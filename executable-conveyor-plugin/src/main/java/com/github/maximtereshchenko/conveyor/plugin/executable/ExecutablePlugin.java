package com.github.maximtereshchenko.conveyor.plugin.executable;

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
import java.util.TreeSet;

public final class ExecutablePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "executable-conveyor-plugin";
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
                "extract-dependencies",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractDependenciesAction(schematic, classesDirectory),
                new TreeSet<>(),
                new TreeSet<>(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "write-manifest",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestAction(classesDirectory, configuration.get("main.class")),
                new TreeSet<>(),
                new TreeSet<>(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "archive-executable",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableAction(classesDirectory, destination),
                new TreeSet<>(Set.of(classesDirectory)),
                new TreeSet<>(Set.of(destination)),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
