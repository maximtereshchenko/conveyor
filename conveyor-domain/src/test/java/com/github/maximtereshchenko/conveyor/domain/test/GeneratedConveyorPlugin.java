package com.github.maximtereshchenko.conveyor.domain.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class GeneratedConveyorPlugin extends GeneratedArtifact {

    GeneratedConveyorPlugin(String name, int version, GeneratedArtifactDefinition... dependencies) {
        super(name, version, List.of(dependencies));
    }

    @Override
    String className() {
        return "ConveyorPluginImpl";
    }

    @Override
    String classSourceCode() {
        return """
            package %s;
            import com.github.maximtereshchenko.conveyor.plugin.api.*;
            import java.io.*;
            import java.nio.file.*;
            import java.util.*;
            import java.util.stream.*;
            public final class ConveyorPluginImpl implements ConveyorPlugin {
                public ConveyorPluginImpl() {
                    %s
                }
                @Override
                public String name() {
                    return "%s";
                }
                @Override
                public Collection<ConveyorTaskBinding> bindings(ConveyorPluginConfiguration configuration) {
                    return List.of(
                        new ConveyorTaskBinding(Stage.COMPILE, () -> writeFile(configuration))
                    );
                }
                private void writeFile(ConveyorPluginConfiguration configuration) {
                    try {
                        Files.writeString(
                            configuration.projectDirectory().resolve("%s-%d-task-executed"),
                            toString(configuration.properties())
                        );
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                private String toString(Map<String, String> properties) {
                    return properties.entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + '=' + entry.getValue())
                        .collect(Collectors.joining(System.lineSeparator()));
                }
            }
            """
            .formatted(packageName(), dependencyUsages(), name(), name(), version());
    }

    @Override
    String moduleInfoSourceCode() {
        return new ModuleInfoSourceCode(
            packageName(),
            Stream.concat(
                    Stream.of("com.github.maximtereshchenko.conveyor.plugin.api"),
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
