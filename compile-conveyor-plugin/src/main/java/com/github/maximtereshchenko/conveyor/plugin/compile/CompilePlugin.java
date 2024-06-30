package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CompilePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "compile-conveyor-plugin";
    }

    @Override
    public List<ConveyorTask> tasks(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        var classesDirectory = configuredPath(configuration, "classes.directory");
        var compiler = new Compiler();
        return List.of(
            compileSourcesConveyorTask(
                "compile-sources",
                Stage.COMPILE,
                Step.RUN,
                configuredPath(configuration, "sources.directory"),
                schematic.classpath(
                    Set.of(DependencyScope.IMPLEMENTATION)
                ),
                classesDirectory,
                compiler
            ),
            new ConveyorTask(
                "publish-exploded-jar-artifact",
                Stage.COMPILE,
                Step.FINALIZE,
                new PublishExplodedJarArtifactTask(classesDirectory),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            compileSourcesConveyorTask(
                "compile-test-sources",
                Stage.TEST,
                Step.PREPARE,
                configuredPath(configuration, "test.sources.directory"),
                Stream.concat(
                        schematic.classpath(
                                Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST)
                            )
                            .stream(),
                        Stream.of(classesDirectory)
                    )
                    .collect(Collectors.toSet()),
                configuredPath(configuration, "test.classes.directory"),
                compiler
            )
        );
    }

    private ConveyorTask compileSourcesConveyorTask(
        String name,
        Stage stage,
        Step step,
        Path sourcesDirectory,
        Set<Path> classpath,
        Path classesDirectory,
        Compiler compiler
    ) {
        return new ConveyorTask(
            name,
            stage,
            step,
            new CompileSourcesAction(
                sourcesDirectory,
                classpath,
                classesDirectory,
                compiler
            ),
            Stream.concat(
                    Stream.of(sourcesDirectory),
                    classpath.stream()
                )
                .map(PathConveyorTaskInput::new)
                .collect(Collectors.toSet()),
            Set.of(new PathConveyorTaskOutput(classesDirectory)),
            Cache.ENABLED
        );
    }

    private Path configuredPath(Map<String, String> configuration, String property) {
        return Paths.get(configuration.get(property)).toAbsolutePath().normalize();
    }
}
