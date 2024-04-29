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

final class CompileTestSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenDiscoverTestsAndCompileTestsBindingsReturned(
        @TempDir Path path
    ) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.TEST, Step.PREPARE), tuple(Stage.TEST, Step.PREPARE));
    }

    @Test
    void givenNoTestSources_whenExecuteTasks_thenNoTestClassProducts(@TempDir Path path)
        throws IOException {
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            class Main {}
            """
        );

        var products = ConveyorTasks.executeTasks(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .build(),
                Map.of()
            )
        );

        assertThat(products)
            .isNotEmpty()
            .extracting(Product::type)
            .doesNotContain(ProductType.TEST_SOURCE, ProductType.EXPLODED_TEST_JAR);
    }

    @Test
    void givenTestSources_whenExecuteTasks_thenTestSourcesAreCompiled(@TempDir Path path)
        throws IOException {
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            class Main {}
            """
        );
        var testJava = srcTestJava(path).resolve("test").resolve("Test.java");
        write(
            testJava,
            """
            package test;
            class Test {}
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of()));

        var mainClass = explodedJar(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        var testClass = explodedTestJar(schematic.constructionDirectory())
            .resolve("test")
            .resolve("Test.class");
        assertThat(mainClass).exists();
        assertThat(testClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedJar(schematic.constructionDirectory()),
                    ProductType.EXPLODED_JAR
                ),
                tuple(testJava, ProductType.TEST_SOURCE),
                tuple(
                    explodedTestJar(schematic.constructionDirectory()),
                    ProductType.EXPLODED_TEST_JAR
                )
            );
    }

    @Test
    void givenTestSourcesDependOnMainModule_whenExecuteTasks_thenTestSourcesAreCompiled(
        @TempDir Path path
    )
        throws IOException {
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            public class Main {}
            """
        );
        var testJava = srcTestJava(path).resolve("test").resolve("Test.java");
        write(
            testJava,
            """
            package test;
            import main.Main;
            class Test {
                void test(){
                    System.out.println(new Main());
                }
            }
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTasks.executeTasks(plugin.bindings(schematic, Map.of()));

        var mainClass = explodedJar(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        var testClass = explodedTestJar(schematic.constructionDirectory())
            .resolve("test")
            .resolve("Test.class");
        assertThat(mainClass).exists();
        assertThat(testClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedJar(schematic.constructionDirectory()),
                    ProductType.EXPLODED_JAR
                ),
                tuple(testJava, ProductType.TEST_SOURCE),
                tuple(
                    explodedTestJar(schematic.constructionDirectory()),
                    ProductType.EXPLODED_TEST_JAR
                )
            );
    }

    @Test
    void givenSourcesFromDifferentSchematics_whenExecuteTasks_thenTestSourcesAreCompiledForCurrentSchematic(
        @TempDir Path path
    ) throws IOException {
        write(
            srcTestJava(path).resolve("test").resolve("Test.java"),
            """
            package test;
            class Test {}
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
                ProductType.TEST_SOURCE
            )
        );

        assertThat(
            explodedTestJar(schematic.constructionDirectory())
                .resolve("test")
                .resolve("Test.class")
        )
            .exists();
    }

    private Path srcTestJava(@TempDir Path path) {
        return path.resolve("src").resolve("test").resolve("java");
    }

    private Path explodedTestJar(@TempDir Path path) {
        return path.resolve("exploded-test-jar");
    }
}