package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        var dependencies = schematic.classpath(Set.of(DependencyScope.IMPLEMENTATION));
        return List.of(
            new ConveyorTask(
                "extract-dependencies",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractDependenciesAction(dependencies, classesDirectory),
                Stream.concat(dependencies.stream(), Stream.of(classesDirectory))
                    .map(PathConveyorTaskInput::new)
                    .collect(Collectors.toSet()),
                Set.of(new PathConveyorTaskOutput(classesDirectory)),
                Cache.ENABLED
            ),
            new ConveyorTask(
                "write-manifest",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestAction(classesDirectory, configuration.get("main.class")),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "archive-executable",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableAction(classesDirectory, destination),
                Set.of(new PathConveyorTaskInput(classesDirectory)),
                Set.of(new PathConveyorTaskOutput(destination)),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
