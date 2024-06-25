package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class CompileTestSourcesTests extends BaseTest {

    @Test
    void givenTestSourcesDependOnMainClasses_whenExecuteTasks_thenTestSourcesAreCompiled(
        @TempDir Path path
    ) throws IOException {
        var sources = path.resolve("sources");
        write(
            sources.resolve("Main.java"),
            """
            package main;
            public class Main {}
            """
        );
        var testSources = path.resolve("test-sources");
        write(
            testSources.resolve("Test.java"),
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
        var classes = path.resolve("classes");
        var testClasses = path.resolve("test-classes");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "sources.directory", sources.toString(),
                    "classes.directory", classes.toString(),
                    "test.sources.directory", testSources.toString(),
                    "test.classes.directory", testClasses.toString()
                )
            )
        );

        assertThat(testClasses.resolve("test").resolve("Test.class")).exists();
    }
}