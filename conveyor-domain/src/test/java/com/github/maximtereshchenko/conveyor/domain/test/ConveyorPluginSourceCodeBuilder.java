package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class ConveyorPluginSourceCodeBuilder {

    private final String name;
    private final int version;
    private final Stage stage;
    private final Collection<String> dependencyClasses;

    private ConveyorPluginSourceCodeBuilder(
        String name,
        int version,
        Stage stage,
        Collection<String> dependencyClasses
    ) {
        this.name = name;
        this.version = version;
        this.stage = stage;
        this.dependencyClasses = List.copyOf(dependencyClasses);
    }

    ConveyorPluginSourceCodeBuilder(ProjectDefinitionBuilder builder) {
        this(builder.name(), builder.version(), Stage.COMPILE, List.of());
    }

    String fullyQualifiedName() {
        return normalizedName() + '.' + normalizedName();
    }

    ConveyorPluginSourceCodeBuilder dependency(String dependencyClass) {
        var copy = new ArrayList<>(dependencyClasses);
        copy.add(dependencyClass);
        return new ConveyorPluginSourceCodeBuilder(name, version, stage, copy);
    }

    ConveyorPluginSourceCodeBuilder name(String name) {
        return new ConveyorPluginSourceCodeBuilder(name, version, stage, dependencyClasses);
    }

    ConveyorPluginSourceCodeBuilder version(int version) {
        return new ConveyorPluginSourceCodeBuilder(name, version, stage, dependencyClasses);
    }

    ConveyorPluginSourceCodeBuilder stage(Stage stage) {
        return new ConveyorPluginSourceCodeBuilder(name, version, stage, dependencyClasses);
    }

    String build() {
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
               public final class %s implements ConveyorPlugin {
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
                                   project.buildDirectory().resolve("%%s-%%s-prepared".formatted(project.name(), fullName))
                               )
                           ),
                           new ConveyorTaskBinding(
                               stage,
                               Step.FINALIZE,
                               (buildFiles) -> writeInstant(
                                   buildFiles,
                                   project.buildDirectory().resolve("%%s-%%s-finalized".formatted(project.name(), fullName))
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
                       return buildFiles.with(write(path), BuildFileType.ARTIFACT);
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
                           project.buildDirectory().resolve("%%s-%%s-configuration".formatted(project.name(), fullName)),
                           toString(configuration.entrySet())
                       );
                       write(
                           project.buildDirectory().resolve("%%s-%%s-module-path-implementation".formatted(project.name(), fullName)),
                           toString(project.modulePath(DependencyScope.IMPLEMENTATION))
                       );
                       write(
                           project.buildDirectory().resolve("%%s-%%s-module-path-test".formatted(project.name(), fullName)),
                           toString(project.modulePath(DependencyScope.TEST))
                       );
                       return writeInstant(buildFiles, project.buildDirectory().resolve("%%s-%%s-run".formatted(project.name(), fullName)));
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
                normalizedName(),
                normalizedName(),
                stage.toString(),
                name, version,
                name,
                dependencyUsages()
            );
    }

    private String normalizedName() {
        return name.toLowerCase(Locale.ROOT).replace("-", "");
    }

    private String dependencyUsages() {
        return dependencyClasses.stream()
            .map("new %s(project.buildDirectory());"::formatted)
            .collect(Collectors.joining());
    }
}
