package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        var classesDirectory = configuredPath(configuration, "classes.directory")
            .orElseGet(() -> classesDirectory(schematic, "classes"));
        var compiler = new Compiler();
        return List.of(
            compileSourcesConveyorTask(
                "compile-sources",
                BindingStage.COMPILE,
                BindingStep.RUN,
                configuredPath(configuration, "sources.directory")
                    .orElseGet(() -> sourcesDirectory(schematic, "main")),
                schematic.classpath(Set.of(ClasspathScope.IMPLEMENTATION)),
                classesDirectory,
                compiler
            ),
            new ConveyorTask(
                "publish-exploded-jar-artifact",
                BindingStage.COMPILE,
                BindingStep.FINALIZE,
                new PublishExplodedJarArtifactTask(classesDirectory, schematic),
                Set.of(),
                Set.of(),
                Cache.DISABLED
            ),
            compileSourcesConveyorTask(
                "compile-test-sources",
                BindingStage.TEST,
                BindingStep.PREPARE,
                configuredPath(configuration, "test.sources.directory")
                    .orElseGet(() -> sourcesDirectory(schematic, "test")),
                Stream.concat(
                        schematic.classpath(
                                Set.of(ClasspathScope.IMPLEMENTATION, ClasspathScope.TEST)
                            )
                            .stream(),
                        Stream.of(classesDirectory)
                    )
                    .collect(Collectors.toSet()),
                configuredPath(configuration, "test.classes.directory")
                    .orElseGet(() -> classesDirectory(schematic, "test-classes")),
                compiler
            )
        );
    }

    private ConveyorTask compileSourcesConveyorTask(
        String name,
        BindingStage stage,
        BindingStep step,
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

    private Path classesDirectory(ConveyorSchematic schematic, String directory) {
        return schematic.path().getParent().resolve(".conveyor").resolve(directory);
    }

    private Path sourcesDirectory(ConveyorSchematic schematic, String sources) {
        return schematic.path().getParent().resolve("src").resolve(sources).resolve("java");
    }

    private Optional<Path> configuredPath(Map<String, String> configuration, String property) {
        var value = configuration.get(property);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Paths.get(value).toAbsolutePath().normalize());
    }
}
