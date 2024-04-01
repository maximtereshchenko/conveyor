package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class GeneratedConveyorPlugin extends GeneratedArtifact {

    private final Stage stage;

    GeneratedConveyorPlugin(
        GsonAdapter gsonAdapter,
        String name,
        int version,
        Stage stage,
        Collection<GeneratedArtifactDefinition> dependencies,
        Collection<GeneratedArtifactDefinition> testDependencies
    ) {
        super(gsonAdapter, name, version, dependencies, testDependencies);
        this.stage = stage;
    }

    GeneratedConveyorPlugin(
        GsonAdapter gsonAdapter,
        String name,
        Stage stage,
        GeneratedArtifactDefinition... dependencies
    ) {
        this(gsonAdapter, name, 1, stage, List.of(dependencies), List.of());
    }

    GeneratedConveyorPlugin(
        GsonAdapter gsonAdapter,
        String name,
        int version
    ) {
        this(gsonAdapter, name, version, Stage.COMPILE, List.of(), List.of());
    }

    GeneratedConveyorPlugin(GsonAdapter gsonAdapter, String name, GeneratedArtifactDefinition... dependencies) {
        this(gsonAdapter, name, Stage.COMPILE, dependencies);
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
                @Override
                public String name() {
                    return "%s";
                }
                @Override
                public Collection<ConveyorTaskBinding> bindings(ConveyorProject project, Map<String, String> configuration) {
                    return List.of(
                        new ConveyorTaskBinding(
                            stage,
                            Step.PREPARE,
                            (buildFiles) -> writeInstant(
                                buildFiles,
                                project.buildDirectory().resolve(fullName + "-prepared")
                            )
                        ),
                        new ConveyorTaskBinding(
                            stage,
                            Step.FINALIZE,
                            (buildFiles) -> writeInstant(
                                buildFiles,
                                project.buildDirectory().resolve(fullName + "-finalized")
                            )
                        ),
                        new ConveyorTaskBinding(
                            stage,
                            Step.RUN,
                            (buildFiles) -> execute(buildFiles, project, configuration)
                        )
                    );
                }
                private BuildFiles writeInstant(BuildFiles buildFiles, Path path) {
                    sleep();
                    return buildFiles.with(new BuildFile(write(path), BuildFileType.ARTIFACT));
                }
                private void sleep() {
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                private BuildFiles execute(BuildFiles buildFiles, ConveyorProject project, Map<String, String> configuration) {
                    %s
                    write(
                        project.buildDirectory().resolve(fullName + "-configuration"),
                        toString(configuration.entrySet())
                    );
                    write(
                        project.buildDirectory().resolve(fullName + "-module-path-implementation"),
                        toString(project.modulePath(DependencyScope.IMPLEMENTATION))
                    );
                    write(
                        project.buildDirectory().resolve(fullName + "-module-path-test"),
                        toString(project.modulePath(DependencyScope.TEST))
                    );
                    return writeInstant(buildFiles, project.buildDirectory().resolve(fullName + "-run"));
                }
                private Path write(Path path) {
                    return write(path, Instant.now().toString());
                }
                private Path write(Path path, String content) {
                    try {
                        return Files.writeString(path, content).toAbsolutePath().normalize();
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
                name(),
                dependencyUsages()
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
            .map("new %s(project.buildDirectory());"::formatted)
            .collect(Collectors.joining());
    }
}
