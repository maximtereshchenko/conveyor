package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class CompileTestSourcesTests {

    private final ConveyorPlugin plugin = new CompilePlugin();

    @Test
    void givenTestSourcesDependOnMainClasses_whenExecuteTasks_thenTestSourcesAreCompiled(
        @TempDir Path path
    ) throws IOException {
        var sources = path.resolve("sources");
        new FileTree(sources.resolve("Main.java"))
            .write("""
                   package main;
                   public class Main {}
                   """);
        var testSources = path.resolve("test-sources");
        new FileTree(testSources.resolve("Test.java"))
            .write("""
                   package test;
                   import main.Main;
                   class Test {
                       void test(){
                           System.out.println(new Main());
                       }
                   }
                   """);
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