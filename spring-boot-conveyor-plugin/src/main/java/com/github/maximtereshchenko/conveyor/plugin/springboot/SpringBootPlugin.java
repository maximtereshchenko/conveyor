package com.github.maximtereshchenko.conveyor.plugin.springboot;

import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.springboot.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        var conveyor = schematic.path().getParent().resolve(".conveyor");
        var containerDirectory = configuredPath(configuration, "container.directory")
            .orElseGet(() -> conveyor.resolve("executable-container"));
        var classpathDirectoryName = "classpath";
        var classpathDirectory = containerDirectory.resolve(classpathDirectoryName);
        return List.of(
            task(
                "copy-classes",
                new CopyClassesAction(
                    configuredPath(configuration, "classes.directory")
                        .orElseGet(() -> conveyor.resolve("classes")),
                    classpathDirectory.resolve("classes")
                )
            ),
            task(
                "copy-dependencies",
                new CopyDependenciesAction(
                    schematic.classpath(Set.of(ClasspathScope.IMPLEMENTATION)),
                    classpathDirectory
                )
            ),
            task(
                "extract-spring-boot-launcher",
                new ExtractSpringBootLauncherAction(containerDirectory)
            ),
            task(
                "write-properties",
                new WritePropertiesAction(
                    containerDirectory.resolve(Configuration.PROPERTIES_CLASS_PATH_LOCATION),
                    classpathDirectoryName,
                    configuration.get("launched.class")
                )
            ),
            task(
                "write-manifest",
                new WriteManifestAction(containerDirectory)
            ),
            task(
                "archive-executable",
                new ArchiveExecutableAction(
                    containerDirectory,
                    configuredPath(configuration, "destination")
                        .orElseGet(() ->
                            conveyor.resolve(
                                "%s-%s-executable.jar".formatted(
                                    schematic.name(),
                                    schematic.version()
                                )
                            )
                        )
                )
            )
        );
    }

    private ConveyorTask task(String name, ConveyorTaskAction action) {
        return new ConveyorTask(
            name,
            BindingStage.ARCHIVE,
            BindingStep.FINALIZE,
            action,
            Set.of(),
            Set.of(),
            Cache.DISABLED
        );
    }

    private Optional<Path> configuredPath(Map<String, String> configuration, String property) {
        return Optional.ofNullable(configuration.get(property))
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .map(Path::normalize);
    }
}
