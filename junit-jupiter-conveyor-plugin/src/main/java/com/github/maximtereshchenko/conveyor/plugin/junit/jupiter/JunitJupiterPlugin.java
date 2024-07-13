package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        var dependencies = schematic.classpath(
            Set.of(ClasspathScope.IMPLEMENTATION, ClasspathScope.TEST)
        );
        return List.of(
            new ConveyorTask(
                "execute-junit-jupiter-tests",
                BindingStage.TEST,
                BindingStep.RUN,
                new RunJunitJupiterTestsAction(
                    classesDirectory,
                    testClassesDirectory,
                    dependencies
                ),
                Stream.concat(
                        Stream.of(classesDirectory, testClassesDirectory),
                        dependencies.stream()
                    )
                    .map(PathConveyorTaskInput::new)
                    .collect(Collectors.toSet()),
                Set.of(),
                Cache.ENABLED
            )
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
