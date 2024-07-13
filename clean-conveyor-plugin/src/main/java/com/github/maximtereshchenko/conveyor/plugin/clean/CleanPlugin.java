package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CleanPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "clean-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        return List.of(
            new ConveyorTask(
                "clean",
                BindingStage.CLEAN,
                BindingStep.RUN,
                new CleanAction(Paths.get(configuration.get("directory"))),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            )
        );
    }
}
