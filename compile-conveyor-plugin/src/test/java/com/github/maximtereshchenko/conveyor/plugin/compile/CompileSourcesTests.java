package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class CompileSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenCompileSourcesAndPublishArtifactBindingsReturned(
        @TempDir Path path
    ) {
        assertThat(
            plugin.bindings(
                new FakeConveyorSchematic(),
                configuration(path)
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(
                tuple(Stage.COMPILE, Step.RUN),
                tuple(Stage.COMPILE, Step.FINALIZE)
            );
    }

    @Test
    void givenNoSources_whenExecuteTasks_thenNoArtifact(@TempDir Path path) {
        var artifacts = ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(),
                configuration(path)
            )
        );

        assertThat(artifacts).isEmpty();
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

        var artifacts = ConveyorTasks.executeTasks(
            plugin.bindings(new FakeConveyorSchematic(), configuration(path))
        );

        assertThat(classes(path).resolve("main").resolve("Main.class")).exists();
        assertThat(artifacts).containsExactly(classes(path));
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
        ConveyorTasks.executeTasks(
            plugin.bindings(new FakeConveyorSchematic(), configuration(dependency))
        );
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

        var artifacts = ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(classes(dependency)),
                configuration(dependent)
            )
        );

        assertThat(classes(dependent).resolve("main").resolve("Main.class")).exists();
        assertThat(artifacts).containsExactly(classes(dependent));
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
        ConveyorTasks.executeTasks(
            plugin.bindings(new FakeConveyorSchematic(), configuration(firstDependency))
        );
        ConveyorTasks.executeTasks(
            plugin.bindings(new FakeConveyorSchematic(), configuration(secondDependency))
        );
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

        var artifacts = ConveyorTasks.executeTasks(
            plugin.bindings(
                new FakeConveyorSchematic(classes(firstDependency), classes(secondDependency)),
                configuration(dependent)
            )
        );

        assertThat(classes(dependent).resolve("main").resolve("Main.class")).exists();
        assertThat(artifacts).containsExactly(classes(dependent));
    }
}