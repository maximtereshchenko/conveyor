package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
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
                new CleanAction(directory(schematic, configuration)),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            )
        );
    }

    private Path directory(ConveyorSchematic schematic, Map<String, String> configuration) {
        var directory = configuration.get("directory");
        if (directory == null) {
            return schematic.path().getParent().resolve(".conveyor");
        }
        return Paths.get(directory).toAbsolutePath().normalize();
    }
}
