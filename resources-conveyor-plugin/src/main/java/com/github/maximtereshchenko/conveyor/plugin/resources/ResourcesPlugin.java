package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class ResourcesPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "resources-conveyor-plugin";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.FINALIZE,
                new CopyResourcesTask(
                    "copy-resources",
                    configuredPath(configuration, "resources.directory"),
                    configuredPath(configuration, "classes.directory")
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new CopyResourcesTask(
                    "copy-test-resources",
                    configuredPath(configuration, "test.resources.directory"),
                    configuredPath(configuration, "test.classes.directory")
                )
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property));
    }
}
