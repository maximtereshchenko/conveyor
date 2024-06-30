package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SpringBootPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "spring-boot-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var containerDirectory = configuredPath(configuration, "container.directory");
        var classpathDirectory = "classpath";
        var destination = configuredPath(configuration, "destination");
        return List.of(
            new ConveyorTask(
                "copy-dependencies",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new CopyClassPathAction(
                    configuredPath(configuration, "classes.directory"),
                    containerDirectory.resolve(classpathDirectory),
                    schematic
                ),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "extract-spring-boot-launcher",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractSpringBootLauncherAction(containerDirectory),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "write-properties",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WritePropertiesAction(
                    containerDirectory.resolve(Configuration.PROPERTIES_CLASS_PATH_LOCATION),
                    classpathDirectory,
                    configuration.get("launched.class")
                ),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "write-manifest",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestAction(containerDirectory),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "archive-executable",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableAction(containerDirectory, destination),
                Set.of(new PathConveyorTaskInput(containerDirectory)),
                Set.of(new PathConveyorTaskOutput(destination)),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
