package com.github.maximtereshchenko.conveyor.domain.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class DependencySourceCodeBuilder {

    private final String name;
    private final int version;
    private final Collection<String> dependencyClasses;

    private DependencySourceCodeBuilder(
        String name,
        int version,
        Collection<String> dependencyClasses
    ) {
        this.name = name;
        this.version = version;
        this.dependencyClasses = List.copyOf(dependencyClasses);
    }

    DependencySourceCodeBuilder(ProjectDefinitionBuilder builder) {
        this(builder.name(), builder.version(), List.of());
    }

    DependencySourceCodeBuilder dependency(String dependencyClass) {
        var copy = new ArrayList<>(dependencyClasses);
        copy.add(dependencyClass);
        return new DependencySourceCodeBuilder(name, version, copy);
    }

    DependencySourceCodeBuilder name(String name) {
        return new DependencySourceCodeBuilder(name, version, dependencyClasses);
    }

    DependencySourceCodeBuilder version(int version) {
        return new DependencySourceCodeBuilder(name, version, dependencyClasses);
    }

    String build() {
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
                normalizedName(),
                normalizedName(),
                normalizedName(),
                dependencyUsages(),
                name, version
            );
    }

    private String normalizedName() {
        return name.toLowerCase(Locale.ROOT).replace("-", "");
    }

    private String dependencyUsages() {
        return dependencyClasses.stream()
            .map("new %s(dir);"::formatted)
            .collect(Collectors.joining());
    }
}
