package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
                    configuredPath(configuration, "resources.directory"),
                    configuredPath(configuration, "classes.directory")
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
                    configuredPath(configuration, "test.resources.directory"),
                    configuredPath(configuration, "test.classes.directory")
                ),
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
