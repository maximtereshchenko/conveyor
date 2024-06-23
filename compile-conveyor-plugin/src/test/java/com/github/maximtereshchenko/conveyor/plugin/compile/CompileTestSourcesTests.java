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

final class CompileTestSourcesTests extends CompilePluginTest {

    @Test
    void givenPlugin_whenBindings_thenDiscoverTestsAndCompileTestsBindingsReturned(
        @TempDir Path path
    ) {
        assertThat(
            plugin.bindings(
                new FakeConveyorSchematic(),
                configuration(path)
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.TEST, Step.PREPARE));
    }

    @Test
    void givenNoTestSources_whenExecuteTasks_thenNoTestClasses(@TempDir Path path)
        throws IOException {
        var mainJava = srcMainJava(path).resolve("main").resolve("Main.java");
        write(
            mainJava,
            """
            package main;
            class Main {}
            """
        );

        ConveyorTasks.executeTasks(
            plugin.bindings(new FakeConveyorSchematic(), configuration(path))
        );

        assertThat(testClasses(path)).doesNotExist();
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

        ConveyorTasks.executeTasks(
            plugin.bindings(new FakeConveyorSchematic(), configuration(path))
        );

        assertThat(classes(path).resolve("main").resolve("Main.class")).exists();
        assertThat(testClasses(path).resolve("test").resolve("Test.class")).exists();
    }

    @Test
    void givenTestSourcesDependOnMainSources_whenExecuteTasks_thenTestSourcesAreCompiled(
        @TempDir Path path
    ) throws IOException {
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

        ConveyorTasks.executeTasks(
            plugin.bindings(new FakeConveyorSchematic(), configuration(path))
        );

        assertThat(classes(path).resolve("main").resolve("Main.class")).exists();
        assertThat(testClasses(path).resolve("test").resolve("Test.class")).exists();
    }
}