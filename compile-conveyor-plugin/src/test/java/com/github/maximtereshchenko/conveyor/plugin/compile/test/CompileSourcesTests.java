package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class CompileSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenDiscoverSourcesAndCompileSourcesBindingsReturned(
        @TempDir Path path
    ) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.COMPILE, Step.PREPARE), tuple(Stage.COMPILE, Step.RUN));
    }

    @Test
    void givenNoSources_whenExecuteTasks_thenNoProducts(@TempDir Path path) {
        var products = ConveyorTasks.executeTasks(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .build(),
                Map.of()
            )
        );

        assertThat(products).isEmpty();
    }

    @Test
    void givenSources_whenExecuteTasks_thenSourcesAreCompiled(@TempDir Path path)
        throws IOException {
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            class Main {}
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of()));

        var mainClass = explodedJar(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedJar(schematic.constructionDirectory()),
                    ProductType.EXPLODED_JAR
                )
            );
    }

    @Test
    void givenDependency_whenExecuteTasks_thenSourcesAreCompiledWithDependency(@TempDir Path path)
        throws IOException {
        var dependency = path.resolve("dependency");
        write(
            srcMainJava(dependency).resolve("dependency").resolve("Dependency.java"),
            """
            package dependency;
            public class Dependency {}
            """
        );
        var dependencySchematic = FakeConveyorSchematicBuilder.discoveryDirectory(dependency)
            .build();
        ConveyorTasks.executeTasks(plugin.bindings(dependencySchematic, Map.of()));
        var dependent = path.resolve("dependent");
        var mainJava = srcMainJava(dependent).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            import dependency.Dependency;
            class Main {
                public static void main(String[] args){
                    System.out.println(new Dependency());
                }
            }
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(dependent)
            .dependency(explodedJar(dependencySchematic.constructionDirectory()))
            .build();

        var products = ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of()));

        var mainClass = explodedJar(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedJar(schematic.constructionDirectory()),
                    ProductType.EXPLODED_JAR
                )
            );
    }

    @Test
    void givenMultipleDependencies_whenExecuteTasks_thenSourcesAreCompiledWithAllDependencies(
        @TempDir Path path
    ) throws IOException {
        var firstDependency = path.resolve("first-dependency");
        write(
            srcMainJava(firstDependency).resolve("firstdependency").resolve("FirstDependency.java"),
            """
            package firstdependency;
            public class FirstDependency {}
            """
        );
        var secondDependency = path.resolve("second-dependency");
        write(
            srcMainJava(secondDependency)
                .resolve("seconddependency")
                .resolve("SecondDependency.java"),
            """
            package seconddependency;
            public class SecondDependency {}
            """
        );
        var firstDependencySchematic = FakeConveyorSchematicBuilder.discoveryDirectory(
                firstDependency
            )
            .build();
        var secondDependencySchematic = FakeConveyorSchematicBuilder.discoveryDirectory(
                secondDependency
            )
            .build();
        ConveyorTasks.executeTasks(plugin.bindings(firstDependencySchematic, Map.of()));
        ConveyorTasks.executeTasks(plugin.bindings(secondDependencySchematic, Map.of()));
        var dependent = path.resolve("dependent");
        var mainJava = srcMainJava(dependent).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            import firstdependency.FirstDependency;
            import seconddependency.SecondDependency;
            class Main {
                public static void main(String[] args){
                    System.out.println(new FirstDependency());
                    System.out.println(new SecondDependency());
                }
            }
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(dependent)
            .dependency(explodedJar(firstDependencySchematic.constructionDirectory()))
            .dependency(explodedJar(secondDependencySchematic.constructionDirectory()))
            .build();

        var products = ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of()));

        var mainClass = explodedJar(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedJar(schematic.constructionDirectory()),
                    ProductType.EXPLODED_JAR
                )
            );
    }

    @Test
    void givenSourcesFromDifferentSchematics_whenExecuteTasks_thenSourcesAreCompiledForCurrentSchematic(
        @TempDir Path path
    ) throws IOException {
        write(
            srcMainJava(path).resolve("main").resolve("Main.java"),
            """
            package main;
            class Main {}
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTasks.executeTasks(
            plugin.bindings(schematic, Map.of()),
            new Product(
                new SchematicCoordinates(
                    "group",
                    "other-schematic",
                    "1.0.0"
                ),
                path.resolve("incorrect"),
                ProductType.SOURCE
            )
        );
        assertThat(
            explodedJar(schematic.constructionDirectory())
                .resolve("main")
                .resolve("Main.class")
        ).exists();
    }
}