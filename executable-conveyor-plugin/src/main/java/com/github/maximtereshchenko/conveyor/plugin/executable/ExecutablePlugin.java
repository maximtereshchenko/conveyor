package com.github.maximtereshchenko.conveyor.plugin.executable;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        var conveyor = schematic.path().getParent().resolve(".conveyor");
        var containerDirectory = configuredPath(configuration, "container.directory")
            .orElseGet(() -> conveyor.resolve("executable-container"));
        var dependencies = schematic.classpath(Set.of(ClasspathScope.IMPLEMENTATION));
        return List.of(
            task(
                "extract-dependencies",
                new ExtractDependenciesAction(dependencies, containerDirectory),
                dependencies.stream()
                    .map(PathConveyorTaskInput::new)
                    .collect(Collectors.toSet()),
                Set.of(new PathConveyorTaskOutput(containerDirectory)),
                Cache.ENABLED
            ),
            task(
                "copy-classes",
                new CopyClassesAction(
                    configuredPath(configuration, "classes.directory")
                        .orElseGet(() -> conveyor.resolve("classes")),
                    containerDirectory
                )
            ),
            task(
                "write-manifest",
                new WriteManifestAction(containerDirectory, configuration.get("main.class"))
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
        return task(name, action, Set.of(), Set.of(), Cache.DISABLED);
    }

    private ConveyorTask task(
        String name,
        ConveyorTaskAction action,
        Set<ConveyorTaskInput> inputs,
        Set<ConveyorTaskOutput> outputs,
        Cache cache
    ) {
        return new ConveyorTask(
            name,
            BindingStage.ARCHIVE,
            BindingStep.FINALIZE,
            action,
            inputs,
            outputs,
            cache
        );
    }

    private Optional<Path> configuredPath(Map<String, String> configuration, String property) {
        return Optional.ofNullable(configuration.get(property))
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .map(Path::normalize);
    }
}
