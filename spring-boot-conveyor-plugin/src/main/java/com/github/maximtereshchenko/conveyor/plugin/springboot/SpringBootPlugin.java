package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
                new TreeSet<>(),
                new TreeSet<>(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "extract-spring-boot-launcher",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ExtractSpringBootLauncherAction(containerDirectory),
                new TreeSet<>(),
                new TreeSet<>(),
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
                new TreeSet<>(),
                new TreeSet<>(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "write-manifest",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new WriteManifestAction(containerDirectory),
                new TreeSet<>(),
                new TreeSet<>(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "archive-executable",
                Stage.ARCHIVE,
                Step.FINALIZE,
                new ArchiveExecutableAction(containerDirectory, destination),
                new TreeSet<>(Set.of(containerDirectory)),
                new TreeSet<>(Set.of(destination)),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
