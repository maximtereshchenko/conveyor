package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class GeneratedConveyorPlugin extends GeneratedArtifact {

    private final Stage stage;

    GeneratedConveyorPlugin(String name, int version, Stage stage, GeneratedArtifactDefinition... dependencies) {
        super(name, version, List.of(dependencies));
        this.stage = stage;
    }

    GeneratedConveyorPlugin(String name, Stage stage, GeneratedArtifactDefinition... dependencies) {
        this(name, 1, stage, dependencies);
    }

    GeneratedConveyorPlugin(String name, GeneratedArtifactDefinition... dependencies) {
        this(name, 1, Stage.COMPILE, dependencies);
    }

    @Override
    String className() {
        return "ConveyorPluginImpl";
    }

    @Override
    String classSourceCode() {
        return """
            package %s;
            import com.github.maximtereshchenko.conveyor.common.api.*;
            import com.github.maximtereshchenko.conveyor.plugin.api.*;
            import java.io.*;
            import java.nio.file.*;
            import java.time.*;
            import java.util.*;
            import java.util.concurrent.*;
            import java.util.stream.*;
            public final class ConveyorPluginImpl implements ConveyorPlugin {
                private final Stage stage = Stage.%s;
                private final String fullName = "%s-%d";
                public ConveyorPluginImpl() {
                    %s
                }
                @Override
                public String name() {
                    return "%s";
                }
                @Override
                public Collection<ConveyorTaskBinding> bindings(Project project, Map<String, String> configuration) {
                    return List.of(
                        new ConveyorTaskBinding(
                            stage,
                            Step.PREPARE,
                            () -> writeInstant(project.projectDirectory().resolve(fullName + "-prepared"))
                        ),
                        new ConveyorTaskBinding(
                            stage,
                            Step.FINALIZE,
                            () -> writeInstant(project.projectDirectory().resolve(fullName + "-finalized"))
                        ),
                        new ConveyorTaskBinding(stage, Step.RUN, () -> execute(project, configuration))
                    );
                }
                private void writeInstant(Path path) {
                    write(path);
                    sleep();
                }
                private void sleep() {
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                private void execute(Project project, Map<String, String> configuration) {
                    write(
                        project.projectDirectory().resolve(fullName + "-configuration"),
                        toString(configuration.entrySet())
                    );
                    write(
                        project.projectDirectory().resolve(fullName + "-module-path-implementation"),
                        toString(project.modulePath(DependencyScope.IMPLEMENTATION))
                    );
                    write(
                        project.projectDirectory().resolve(fullName + "-module-path-test"),
                        toString(project.modulePath(DependencyScope.TEST))
                    );
                    writeInstant(project.projectDirectory().resolve(fullName + "-run"));
                }
                private void write(Path path) {
                    write(path, Instant.now().toString());
                }
                private void write(Path path, String content) {
                    try {
                        Files.writeString(path, content);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                private String toString(Collection<?> objects) {
                    return objects.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining(System.lineSeparator()));
                }
            }
            """
            .formatted(
                packageName(),
                stage.toString(),
                name(), version(),
                dependencyUsages(),
                name()
            );
    }

    @Override
    String moduleInfoSourceCode() {
        return new ModuleInfoSourceCode(
            packageName(),
            Stream.concat(
                    Stream.of(
                        "com.github.maximtereshchenko.conveyor.plugin.api",
                        "com.github.maximtereshchenko.conveyor.common.api"
                    ),
                    dependencies()
                        .stream()
                        .map(GeneratedArtifactDefinition::moduleName)
                )
                .toList(),
            Map.of(
                "com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin",
                packageName() + '.' + className()
            ),
            List.of()
        )
            .toString();
    }

    private String dependencyUsages() {
        return dependencies()
            .stream()
            .map(GeneratedArtifactDefinition::className)
            .map("new %s();"::formatted)
            .collect(Collectors.joining());
    }
}
