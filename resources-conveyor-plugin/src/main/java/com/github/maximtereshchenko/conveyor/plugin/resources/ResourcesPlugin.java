package com.github.maximtereshchenko.conveyor.plugin.resources;

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
                Stage.COMPILE,
                Step.FINALIZE,
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
                Stage.TEST,
                Step.PREPARE,
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
