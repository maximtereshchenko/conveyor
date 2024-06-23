package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class JunitJupiterPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "junit-jupiter-conveyor-plugin";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.RUN,
                new RunJunitJupiterTestsTask(
                    configuredPath(configuration, "test.classes.directory"),
                    configuredPath(configuration, "classes.directory"),
                    schematic
                )
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property));
    }
}
