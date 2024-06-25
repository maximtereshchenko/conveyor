package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class CompileSourcesTests extends BaseTest {

    @Test
    void givenNoSources_whenExecuteTasks_thenNoArtifact(@TempDir Path path) throws IOException {
        var nonExistent = path.resolve("non-existent");

        var artifacts = ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "sources.directory", path.resolve("sources").toString(),
                    "classes.directory", path.resolve("classes").toString(),
                    "test.sources.directory", nonExistent.toString(),
                    "test.classes.directory", nonExistent.toString()
                )
            )
        );

        assertThat(artifacts).isEmpty();
    }

    @Test
    void givenSources_whenExecuteTasks_thenSourcesAreCompiled(@TempDir Path path)
        throws IOException {
        var sources = path.resolve("sources");
        write(
            sources.resolve("Main.java"),
            """
            package main;
            class Main {}
            """
        );
        var classes = path.resolve("classes");
        var nonExistent = path.resolve("non-existent");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "sources.directory", sources.toString(),
                    "classes.directory", classes.toString(),
                    "test.sources.directory", nonExistent.toString(),
                    "test.classes.directory", nonExistent.toString()
                )
            )
        );

        assertThat(classes.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenDependency_whenExecuteTasks_thenSourcesAreCompiledWithDependency(@TempDir Path path)
        throws IOException {
        var dependencySources = path.resolve("dependency-sources");
        write(
            dependencySources.resolve("Dependency.java"),
            """
            package dependency;
            public class Dependency {}
            """
        );
        var dependencyClasses = path.resolve("dependency-classes");
        var nonExistent = path.resolve("non-existent");
        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "sources.directory", dependencySources.toString(),
                    "classes.directory", dependencyClasses.toString(),
                    "test.sources.directory", nonExistent.toString(),
                    "test.classes.directory", nonExistent.toString()
                )
            )
        );
        var dependentSources = path.resolve("dependent-sources");
        write(
            dependentSources.resolve("Main.java"),
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
        var dependentClasses = path.resolve("dependent-classes");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path, dependencyClasses),
                Map.of(
                    "sources.directory", dependentSources.toString(),
                    "classes.directory", dependentClasses.toString(),
                    "test.sources.directory", nonExistent.toString(),
                    "test.classes.directory", nonExistent.toString()
                )
            )
        );

        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenMultipleDependencies_whenExecuteTasks_thenSourcesAreCompiledWithAllDependencies(
        @TempDir Path path
    ) throws IOException {
        var firstDependencySources = path.resolve("first-dependency-sources");
        write(
            firstDependencySources.resolve("FirstDependency.java"),
            """
            package firstdependency;
            public class FirstDependency {}
            """
        );
        var firstDependencyClasses = path.resolve("first-dependency-classes");
        var secondDependencySources = path.resolve("second-dependency-sources");
        write(
            secondDependencySources.resolve("SecondDependency.java"),
            """
            package seconddependency;
            public class SecondDependency {}
            """
        );
        var secondDependencyClasses = path.resolve("second-dependency-classes");
        var schematic = FakeConveyorSchematic.from(path);
        var nonExistent = path.resolve("non-existent");
        ConveyorTasks.executeTasks(
            plugin.tasks(
                schematic,
                Map.of(
                    "sources.directory", firstDependencySources.toString(),
                    "classes.directory", firstDependencyClasses.toString(),
                    "test.sources.directory", nonExistent.toString(),
                    "test.classes.directory", nonExistent.toString()
                )
            )
        );
        ConveyorTasks.executeTasks(
            plugin.tasks(
                schematic,
                Map.of(
                    "sources.directory", secondDependencySources.toString(),
                    "classes.directory", secondDependencyClasses.toString(),
                    "test.sources.directory", nonExistent.toString(),
                    "test.classes.directory", nonExistent.toString()
                )
            )
        );
        var dependentSources = path.resolve("dependent-sources");
        write(
            dependentSources.resolve("Main.java"),
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
        var dependentClasses = path.resolve("dependent-classes");

        ConveyorTasks.executeTasks(
            plugin.tasks(
                FakeConveyorSchematic.from(path, firstDependencyClasses, secondDependencyClasses),
                Map.of(
                    "sources.directory", dependentSources.toString(),
                    "classes.directory", dependentClasses.toString(),
                    "test.sources.directory", nonExistent.toString(),
                    "test.classes.directory", nonExistent.toString()
                )
            )
        );

        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }
}