package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class CompileSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenDiscoverSourcesAndCompileSourcesBindingsReturned(Path path) {
        assertThat(bindings(path))
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.COMPILE, Step.PREPARE), tuple(Stage.COMPILE, Step.RUN));
    }

    @Test
    void givenNoSources_whenExecuteTasks_thenNoProducts(Path path) {
        assertThat(executeTasks(path)).isEmpty();
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

        var products = executeTasks(path);

        var mainClass = explodedModule(path).resolve("main").resolve("Main.class");
        assertThat(moduleInfoClass(explodedModule(path))).exists();
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(path)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(explodedModule(path), ProductType.EXPLODED_MODULE)
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
        executeTasks(dependency);
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

        var products = executeTasks(dependent, explodedModule(dependency));

        var mainClass = explodedModule(dependent).resolve("main").resolve("Main.class");
        assertThat(moduleInfoClass(explodedModule(dependent))).exists();
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(dependent)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(explodedModule(dependent), ProductType.EXPLODED_MODULE)
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
        executeTasks(firstDependency);
        executeTasks(secondDependency);
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

        var products = executeTasks(
            dependent,
            explodedModule(firstDependency),
            explodedModule(secondDependency)
        );

        var mainClass = explodedModule(dependent).resolve("main").resolve("Main.class");
        assertThat(moduleInfoClass(explodedModule(dependent))).exists();
        assertThat(mainClass).exists();
        assertThat(products)
            .extracting(Product::path, Product::type)
            .containsOnly(
                tuple(moduleInfoJava(srcMainJava(dependent)), ProductType.SOURCE),
                tuple(mainJava, ProductType.SOURCE),
                tuple(explodedModule(dependent), ProductType.EXPLODED_MODULE)
            );
    }
}