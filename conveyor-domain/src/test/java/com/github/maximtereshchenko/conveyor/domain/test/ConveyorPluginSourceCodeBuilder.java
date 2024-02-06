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
                   public List<ConveyorTaskBinding> bindings(ConveyorProperties properties, Map<String, String> configuration) {
                       return List.of(
                           new ConveyorTaskBinding(
                               stage,
                               Step.PREPARE,
                               (dependencies, products) -> writeInstant(
                                   products,
                                   properties.constructionDirectory(),
                                   "%%s-%%s-prepared".formatted(properties.schematicName(), fullName)
                               )
                           ),
                           new ConveyorTaskBinding(
                               stage,
                               Step.FINALIZE,
                               (dependencies, products) -> writeInstant(
                                   products,
                                   properties.constructionDirectory(),
                                   "%%s-%%s-finalized".formatted(properties.schematicName(), fullName)
                               )
                           ),
                           new ConveyorTaskBinding(
                               stage,
                               Step.RUN,
                               (dependencies, products) -> execute(dependencies, products, properties, configuration)
                           )
                       );
                   }
                   private Products writeInstant(Products products, Path directory, String name) {
                       sleep();
                       return products.with(write(directory, name), ProductType.MODULE_COMPONENT);
                   }
                   private void sleep() {
                       try {
                           TimeUnit.MILLISECONDS.sleep(10);
                       } catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                       }
                   }
                   private Products execute(ConveyorSchematicDependencies dependencies, Products products, ConveyorProperties properties, Map<String, String> configuration) {
                       %s
                       write(
                           properties.constructionDirectory(),
                           "%%s-%%s-configuration".formatted(properties.schematicName(), fullName),
                           toString(configuration.entrySet())
                       );
                       write(
                           properties.constructionDirectory(),
                           "%%s-%%s-module-path-implementation".formatted(properties.schematicName(), fullName),
                           toString(dependencies.modulePath(DependencyScope.IMPLEMENTATION))
                       );
                       write(
                           properties.constructionDirectory(),
                           "%%s-%%s-module-path-test".formatted(properties.schematicName(), fullName),
                           toString(dependencies.modulePath(DependencyScope.TEST))
                       );
                       return writeInstant(
                            products,
                            properties.constructionDirectory(),
                            "%%s-%%s-run".formatted(properties.schematicName(), fullName)
                       )
                       .with(properties.discoveryDirectory(), ProductType.MODULE);
                   }
                   private Path write(Path directory, String name) {
                       return write(directory, name, Instant.now().toString());
                   }
                   private Path write(Path directory, String name, String content) {
                       try {
                           return Files.writeString(Files.createDirectories(directory).resolve(name), content).toAbsolutePath().normalize();
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
            .map("new %s(properties.constructionDirectory());"::formatted)
            .collect(Collectors.joining());
    }
}
