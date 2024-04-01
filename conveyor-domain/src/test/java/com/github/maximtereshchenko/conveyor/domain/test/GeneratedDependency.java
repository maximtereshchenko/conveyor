package com.github.maximtereshchenko.conveyor.domain.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class GeneratedDependency extends GeneratedArtifact {

    GeneratedDependency(String name, int version, GeneratedArtifactDefinition... dependencies) {
        super(name, version, List.of(dependencies));
    }

    GeneratedDependency(String name, GeneratedArtifactDefinition... dependencies) {
        this(name, 1, dependencies);
    }

    @Override
    String className() {
        return capitalized(packageName());
    }

    @Override
    String classSourceCode() {
        return """
            package %s;
            import java.io.*;
            import java.nio.file.*;
            public final class %s {
                public %s(Path dir) {
                    %s
                    try {
                        var path = dir.resolve("%s-%d");
                        if (!Files.exists(path)) Files.createFile(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
            """
            .formatted(
                packageName(),
                className(),
                className(),
                dependencyUsages(),
                name(), version()
            );
    }

    @Override
    String moduleInfoSourceCode() {
        return new ModuleInfoSourceCode(
            packageName(),
            dependencies()
                .stream()
                .map(GeneratedArtifactDefinition::moduleName)
                .toList(),
            Map.of(),
            List.of(packageName())
        ).toString();
    }

    private String capitalized(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    private String dependencyUsages() {
        return dependencies()
            .stream()
            .map(GeneratedArtifactDefinition::className)
            .map("new %s(dir);"::formatted)
            .collect(Collectors.joining());
    }
}
