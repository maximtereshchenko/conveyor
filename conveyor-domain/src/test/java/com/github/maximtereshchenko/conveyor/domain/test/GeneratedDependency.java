package com.github.maximtereshchenko.conveyor.domain.test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class GeneratedDependency extends GeneratedArtifact {

    private final Path directory;

    GeneratedDependency(Path directory, String name, int version, GeneratedArtifactDefinition... dependencies) {
        super(name, version, List.of(dependencies));
        this.directory = directory;
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
                public %s() {
                    %s
                    try {
                        var path = Paths.get("%s");
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
                directory.resolve(name() + '-' + version())
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
            .map("new %s();"::formatted)
            .collect(Collectors.joining());
    }
}
