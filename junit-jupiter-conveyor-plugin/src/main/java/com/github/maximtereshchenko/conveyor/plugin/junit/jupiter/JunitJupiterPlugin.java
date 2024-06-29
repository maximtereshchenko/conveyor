package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

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
import java.util.TreeSet;

public final class JunitJupiterPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "junit-jupiter-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var testClassesDirectory = configuredPath(configuration, "test.classes.directory");
        var classesDirectory = configuredPath(configuration, "classes.directory");
        return List.of(
            new ConveyorTask(
                "execute-junit-jupiter-tests",
                Stage.TEST,
                Step.RUN,
                new RunJunitJupiterTestsAction(
                    testClassesDirectory,
                    classesDirectory,
                    schematic
                ),
                new TreeSet<>(Set.of(testClassesDirectory, classesDirectory)),
                new TreeSet<>(),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
