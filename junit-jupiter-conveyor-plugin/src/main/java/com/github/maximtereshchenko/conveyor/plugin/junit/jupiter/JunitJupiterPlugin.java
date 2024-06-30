package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                Set.of(
                    new PathConveyorTaskInput(testClassesDirectory),
                    new PathConveyorTaskInput(classesDirectory)
                ),
                Set.of(),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
