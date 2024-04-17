package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class CompileTestSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenDiscoverTestsAndCompileTestsBindingsReturned(Path path) {
        ConveyorTaskBindings.from(FakeConveyorSchematicBuilder.discoveryDirectory(path).build())
            .assertThat()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.TEST, Step.PREPARE), tuple(Stage.TEST, Step.PREPARE));
    }

    @Test
    void givenNoTestSources_whenExecuteTasks_thenNoTestClassProducts(Path path) throws IOException {
        var moduleInfoJava = srcMainJava(path).resolve("module-info.java");
        write(moduleInfoJava, "module Main {}");
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            class Main {}
            """
        );

        var products = ConveyorTaskBindings.from(
                FakeConveyorSchematicBuilder.discoveryDirectory(path)
                    .build()
            )
            .executeTasks();

        assertThat(products)
            .isNotEmpty()
            .extracting(Product::type)
            .doesNotContain(ProductType.TEST_SOURCE, ProductType.EXPLODED_TEST_MODULE);
    }

    @Test
    void givenTestSources_whenExecuteTasks_thenTestSourcesAreCompiled(Path path)
        throws IOException {
        write(moduleInfoJava(srcMainJava(path)), "module main {}");
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            class Main {}
            """
        );
        write(moduleInfoJava(srcTestJava(path)), "module main.test {}");
        var testJava = srcTestJava(path).resolve("test").resolve("Test.java");
        write(
            testJava,
            """
            package test;
            class Test {}
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        var products = ConveyorTaskBindings.from(schematic).executeTasks();

        var mainClass = explodedModule(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        var testClass = explodedTestModule(schematic.constructionDirectory())
            .resolve("test")
            .resolve("Test.class");
        assertThat(moduleInfoClass(explodedModule(schematic.constructionDirectory()))).exists();
        assertThat(mainClass).exists();
        assertThat(moduleInfoClass(explodedTestModule(schematic.constructionDirectory()))).exists();
        assertThat(testClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(path)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedModule(schematic.constructionDirectory()),
                    ProductType.EXPLODED_MODULE
                ),
                tuple(moduleInfoJava(srcTestJava(path)), ProductType.TEST_SOURCE),
                tuple(testJava, ProductType.TEST_SOURCE),
                tuple(
                    explodedTestModule(schematic.constructionDirectory()),
                    ProductType.EXPLODED_TEST_MODULE
                )
            );
    }

    @Test
    void givenTestSourcesDependOnMainModule_whenExecuteTasks_thenTestSourcesAreCompiled(Path path)
        throws IOException {
        write(
            moduleInfoJava(srcMainJava(path)),
            """
            module main {
                exports main;
            }
            """
        );
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            public class Main {}
            """
        );
        write(
            moduleInfoJava(srcTestJava(path)),
            """
            module main.test {
                requires main;
            }
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

        var products = ConveyorTaskBindings.from(schematic).executeTasks();

        var mainClass = explodedModule(schematic.constructionDirectory())
            .resolve("main")
            .resolve("Main.class");
        var testClass = explodedTestModule(schematic.constructionDirectory())
            .resolve("test")
            .resolve("Test.class");
        assertThat(moduleInfoClass(explodedModule(schematic.constructionDirectory()))).exists();
        assertThat(mainClass).exists();
        assertThat(moduleInfoClass(explodedTestModule(schematic.constructionDirectory()))).exists();
        assertThat(testClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(path)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(
                    explodedModule(schematic.constructionDirectory()),
                    ProductType.EXPLODED_MODULE
                ),
                tuple(moduleInfoJava(srcTestJava(path)), ProductType.TEST_SOURCE),
                tuple(testJava, ProductType.TEST_SOURCE),
                tuple(
                    explodedTestModule(schematic.constructionDirectory()),
                    ProductType.EXPLODED_TEST_MODULE
                )
            );
    }

    @Test
    void givenSourcesFromDifferentSchematics_whenExecuteTasks_thenTestSourcesAreCompiledForCurrentSchematic(
        Path path
    ) throws IOException {
        write(
            srcTestJava(path).resolve("test").resolve("Test.java"),
            """
            package test;
            class Test {}
            """
        );
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();

        ConveyorTaskBindings.from(schematic)
            .executeTasks(
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
            explodedTestModule(schematic.constructionDirectory())
                .resolve("test")
                .resolve("Test.class")
        )
            .exists();
    }

    private Path srcTestJava(Path path) {
        return path.resolve("src").resolve("test").resolve("java");
    }

    private Path explodedTestModule(Path path) {
        return path.resolve("exploded-test-module");
    }
}