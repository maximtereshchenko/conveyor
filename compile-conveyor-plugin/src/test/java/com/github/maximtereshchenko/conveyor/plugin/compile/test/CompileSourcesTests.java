package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class CompileSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenDiscoverSourcesAndCompileSourcesBindingsReturned(Path path) {
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
    void givenNoSources_whenExecuteTasks_thenNoProducts(Path path) {
        var products = ConveyorTasks.executeTasks(
            FakeConveyorSchematicBuilder.discoveryDirectory(path)
                .build(),
            plugin
        );

        assertThat(products).isEmpty();
    }

    @Test
    void givenSources_whenExecuteTasks_thenSourcesAreCompiled(Path path) throws IOException {
        write(moduleInfoJava(srcMainJava(path)), "module main {}");
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            class Main {}
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(schematic, plugin);

        var mainClass = explodedModule(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        assertThat(moduleInfoClass(explodedModule(schematic.constructionDirectory()))).exists();
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(path)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedModule(schematic.constructionDirectory()),
                    ProductType.EXPLODED_MODULE
                )
            );
    }

    @Test
    void givenDependency_whenExecuteTasks_thenSourcesAreCompiledWithDependency(Path path)
        throws IOException {
        var dependency = path.resolve("dependency");
        write(
            moduleInfoJava(srcMainJava(dependency)),
            """
            module dependency {
                exports dependency;
            }
            """
        );
        write(
            srcMainJava(dependency).resolve("dependency").resolve("Dependency.java"),
            """
            package dependency;
            public class Dependency {}
            """
        );
        var dependencySchematic = FakeConveyorSchematicBuilder.discoveryDirectory(dependency)
            .build();
        ConveyorTasks.executeTasks(dependencySchematic, plugin);
        var dependent = path.resolve("dependent");
        write(
            moduleInfoJava(srcMainJava(dependent)),
            """
            module main {
                requires dependency;
            }
            """
        );
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
            .dependency(explodedModule(dependencySchematic.constructionDirectory()))
            .build();

        var products = ConveyorTasks.executeTasks(schematic, plugin);

        var mainClass = explodedModule(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        assertThat(moduleInfoClass(explodedModule(schematic.constructionDirectory()))).exists();
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(dependent)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedModule(schematic.constructionDirectory()),
                    ProductType.EXPLODED_MODULE
                )
            );
    }

    @Test
    void givenMultipleDependencies_whenExecuteTasks_thenSourcesAreCompiledWithAllDependencies(
        Path path
    ) throws IOException {
        var firstDependency = path.resolve("first-dependency");
        write(
            moduleInfoJava(srcMainJava(firstDependency)),
            """
            module firstdependency {
                exports firstdependency;
            }
            """
        );
        write(
            srcMainJava(firstDependency).resolve("firstdependency").resolve("FirstDependency.java"),
            """
            package firstdependency;
            public class FirstDependency {}
            """
        );
        var secondDependency = path.resolve("second-dependency");
        write(
            moduleInfoJava(srcMainJava(secondDependency)),
            """
            module seconddependency {
                exports seconddependency;
            }
            """
        );
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
        ConveyorTasks.executeTasks(firstDependencySchematic, plugin);
        ConveyorTasks.executeTasks(secondDependencySchematic, plugin);
        var dependent = path.resolve("dependent");
        write(
            moduleInfoJava(srcMainJava(dependent)),
            """
            module main {
                requires firstdependency;
                requires seconddependency;
            }
            """
        );
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
            .dependency(explodedModule(firstDependencySchematic.constructionDirectory()))
            .dependency(explodedModule(secondDependencySchematic.constructionDirectory()))
            .build();

        var products = ConveyorTasks.executeTasks(schematic, plugin);

        var mainClass = explodedModule(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        assertThat(moduleInfoClass(explodedModule(schematic.constructionDirectory()))).exists();
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(dependent)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedModule(schematic.constructionDirectory()),
                    ProductType.EXPLODED_MODULE
                )
            );
    }

    @Test
    void givenSourcesFromDifferentSchematics_whenExecuteTasks_thenSourcesAreCompiledForCurrentSchematic(
        Path path
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
            schematic,
            plugin,
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
            explodedModule(schematic.constructionDirectory())
                .resolve("main")
                .resolve("Main.class")
        ).exists();
    }
}