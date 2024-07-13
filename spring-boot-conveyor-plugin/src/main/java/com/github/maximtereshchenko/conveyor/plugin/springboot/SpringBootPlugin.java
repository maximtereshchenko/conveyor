package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        var classesDirectory = configuredPath(configuration, "classes.directory");
        var containerDirectory = configuredPath(configuration, "container.directory");
        var classpathDirectory = "classpath";
        var classpathDestination = containerDirectory.resolve(classpathDirectory);
        var dependencies = schematic.classpath(Set.of(ClasspathScope.IMPLEMENTATION));
        var destination = configuredPath(configuration, "destination");
        return List.of(
            new ConveyorTask(
                "copy-classpath",
                BindingStage.ARCHIVE,
                BindingStep.FINALIZE,
                new CopyClasspathAction(classesDirectory, dependencies, classpathDestination),
                Stream.concat(Stream.of(classesDirectory), dependencies.stream())
                    .map(PathConveyorTaskInput::new)
                    .collect(Collectors.toSet()),
                Set.of(new PathConveyorTaskOutput(classpathDestination)),
                Cache.ENABLED //TODO disable
            ),
            new ConveyorTask(
                "extract-spring-boot-launcher",
                BindingStage.ARCHIVE,
                BindingStep.FINALIZE,
                new ExtractSpringBootLauncherAction(containerDirectory),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "write-properties",
                BindingStage.ARCHIVE,
                BindingStep.FINALIZE,
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
                BindingStage.ARCHIVE,
                BindingStep.FINALIZE,
                new WriteManifestAction(containerDirectory),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "archive-executable",
                BindingStage.ARCHIVE,
                BindingStep.FINALIZE,
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
