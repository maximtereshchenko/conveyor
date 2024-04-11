package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@ExtendWith(JimfsExtension.class)
final class CompileTestSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenDiscoverTestsAndCompileTestsBindingsReturned(Path path) {
        assertThat(bindings(path))
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

        assertThat(executeTasks(path))
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

        var products = executeTasks(path);

        var mainClass = explodedModule(path).resolve("main").resolve("Main.class");
        var testClass = explodedTestModule(path).resolve("test").resolve("Test.class");
        assertThat(moduleInfoClass(explodedModule(path))).exists();
        assertThat(mainClass).exists();
        assertThat(moduleInfoClass(explodedTestModule(path))).exists();
        assertThat(testClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(path)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(explodedModule(path), ProductType.EXPLODED_MODULE),
                tuple(moduleInfoJava(srcTestJava(path)), ProductType.TEST_SOURCE),
                tuple(testJava, ProductType.TEST_SOURCE),
                tuple(explodedTestModule(path), ProductType.EXPLODED_TEST_MODULE)
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

        var products = executeTasks(path);

        var mainClass = explodedModule(path).resolve("main").resolve("Main.class");
        var testClass = explodedTestModule(path).resolve("test").resolve("Test.class");
        assertThat(moduleInfoClass(explodedModule(path))).exists();
        assertThat(mainClass).exists();
        assertThat(moduleInfoClass(explodedTestModule(path))).exists();
        assertThat(testClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(path)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(explodedModule(path), ProductType.EXPLODED_MODULE),
                tuple(moduleInfoJava(srcTestJava(path)), ProductType.TEST_SOURCE),
                tuple(testJava, ProductType.TEST_SOURCE),
                tuple(explodedTestModule(path), ProductType.EXPLODED_TEST_MODULE)
            );
    }

    private Path srcTestJava(Path path) {
        return path.resolve("src").resolve("test").resolve("java");
    }

    private Path explodedTestModule(Path path) {
        return constructionDirectory(path).resolve("exploded-test-module");
    }
}