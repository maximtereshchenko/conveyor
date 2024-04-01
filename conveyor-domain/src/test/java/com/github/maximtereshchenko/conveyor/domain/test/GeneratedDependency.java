package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class GeneratedDependency extends GeneratedArtifact {

    GeneratedDependency(
        GsonAdapter gsonAdapter,
        String name,
        int version,
        Collection<GeneratedArtifactDefinition> dependencies
    ) {
        super(gsonAdapter, name, version, dependencies);
    }

    GeneratedDependency(
        GsonAdapter gsonAdapter,
        String name,
        int version,
        GeneratedArtifactDefinition... dependencies
    ) {
        this(gsonAdapter, name, version, List.of(dependencies));
    }

    GeneratedDependency(GsonAdapter gsonAdapter, String name, GeneratedArtifactDefinition... dependencies) {
        this(gsonAdapter, name, 1, List.of(dependencies));
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
