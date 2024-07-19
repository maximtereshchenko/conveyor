package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ResourcesPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "resources-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        return List.of(
            new ConveyorTask(
                "copy-resources",
                BindingStage.COMPILE,
                BindingStep.FINALIZE,
                new CopyResourcesAction(
                    configuredPath(configuration, "resources.directory")
                        .orElseGet(() -> resources(schematic, "main")),
                    configuredPath(configuration, "resources.destination.directory")
                        .orElseGet(() -> classes(schematic, "classes"))
                ),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            new ConveyorTask(
                "copy-test-resources",
                BindingStage.TEST,
                BindingStep.PREPARE,
                new CopyResourcesAction(
                    configuredPath(configuration, "test.resources.directory")
                        .orElseGet(() -> resources(schematic, "test")),
                    configuredPath(configuration, "test.resources.destination.directory")
                        .orElseGet(() -> classes(schematic, "test-classes"))
                ),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            )
        );
    }

    private Path resources(ConveyorSchematic schematic, String sources) {
        return schematic.path().getParent().resolve("src").resolve(sources).resolve("resources");
    }

    private Path classes(ConveyorSchematic schematic, String classes) {
        return schematic.path().getParent().resolve(".conveyor").resolve(classes);
    }

    private Optional<Path> configuredPath(Map<String, String> configuration, String property) {
        return Optional.ofNullable(configuration.get(property))
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .map(Path::normalize);
    }
}
