package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        var testClassesDirectory = configuredPath(configuration, "test.classes.directory")
            .orElseGet(() -> classes(schematic, "classes"));
        var classesDirectory = configuredPath(configuration, "classes.directory")
            .orElseGet(() -> classes(schematic, "test-classes"));
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

    private Path classes(ConveyorSchematic schematic, String directory) {
        return schematic.path().getParent().resolve(".conveyor").resolve(directory);
    }

    private Optional<Path> configuredPath(Map<String, String> configuration, String property) {
        return Optional.ofNullable(configuration.get(property))
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .map(Path::normalize);
    }
}
