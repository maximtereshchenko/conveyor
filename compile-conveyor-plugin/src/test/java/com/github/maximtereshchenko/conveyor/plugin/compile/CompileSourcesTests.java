package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class CompileSourcesTests {

    @Test
    void givenNoSources_whenExecuteTasks_thenNoArtifact(@TempDir Path path) throws IOException {
        var nonExistent = path.resolve("non-existent");

        new Dsl(new CompilePlugin(), path)
            .givenConfiguration("sources.directory", path.resolve("sources"))
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("test.sources.directory", nonExistent)
            .givenConfiguration("test.classes.directory", nonExistent)
            .tasks()
            .execute()
            .thenNoArtifactPublished();
    }

    @Test
    void givenSources_whenExecuteTasks_thenSourcesAreCompiled(@TempDir Path path)
        throws IOException {
        var sources = path.resolve("sources");
        new FileTree(sources.resolve("Main.java"))
            .write("""
                   package main;
                   class Main {}
                   """);
        var classes = path.resolve("classes");
        var nonExistent = path.resolve("non-existent");

        new Dsl(new CompilePlugin(), path)
            .givenConfiguration("sources.directory", sources)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("test.sources.directory", nonExistent)
            .givenConfiguration("test.classes.directory", nonExistent)
            .tasks()
            .execute();

        assertThat(classes.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenDependency_whenExecuteTasks_thenSourcesAreCompiledWithDependency(@TempDir Path path)
        throws IOException {
        var dependencySources = path.resolve("dependency-sources");
        new FileTree(dependencySources.resolve("Dependency.java"))
            .write("""
                   package dependency;
                   public class Dependency {}
                   """);
        var dependencyClasses = path.resolve("dependency-classes");
        var nonExistent = path.resolve("non-existent");
        var dsl = new Dsl(new CompilePlugin(), path)
            .givenConfiguration("test.sources.directory", nonExistent)
            .givenConfiguration("test.classes.directory", nonExistent);
        dsl.givenConfiguration("sources.directory", dependencySources)
            .givenConfiguration("classes.directory", dependencyClasses)
            .tasks()
            .execute();
        var dependentSources = path.resolve("dependent-sources");
        new FileTree(dependentSources.resolve("Main.java"))
            .write("""
                   package main;
                   import dependency.Dependency;
                   class Main {
                       public static void main(String[] args){
                           System.out.println(new Dependency());
                       }
                   }
                   """);
        var dependentClasses = path.resolve("dependent-classes");

        dsl.givenDependency(dependencyClasses)
            .givenConfiguration("sources.directory", dependentSources)
            .givenConfiguration("classes.directory", dependentClasses)
            .tasks()
            .execute();

        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenMultipleDependencies_whenExecuteTasks_thenSourcesAreCompiledWithAllDependencies(
        @TempDir Path path
    ) throws IOException {
        var firstDependencySources = path.resolve("first-dependency-sources");
        new FileTree(firstDependencySources.resolve("FirstDependency.java"))
            .write("""
                   package firstdependency;
                   public class FirstDependency {}
                   """);
        var firstDependencyClasses = path.resolve("first-dependency-classes");
        var secondDependencySources = path.resolve("second-dependency-sources");
        new FileTree(secondDependencySources.resolve("SecondDependency.java"))
            .write("""
                   package seconddependency;
                   public class SecondDependency {}
                   """);
        var secondDependencyClasses = path.resolve("second-dependency-classes");
        var nonExistent = path.resolve("non-existent");
        var dsl = new Dsl(new CompilePlugin(), path)
            .givenConfiguration("test.sources.directory", nonExistent)
            .givenConfiguration("test.classes.directory", nonExistent);
        dsl.givenConfiguration("sources.directory", firstDependencySources)
            .givenConfiguration("classes.directory", firstDependencyClasses)
            .tasks()
            .execute();
        dsl.givenConfiguration("sources.directory", secondDependencySources)
            .givenConfiguration("classes.directory", secondDependencyClasses)
            .tasks()
            .execute();
        var dependentSources = path.resolve("dependent-sources");
        new FileTree(dependentSources.resolve("Main.java"))
            .write("""
                   package main;
                   import firstdependency.FirstDependency;
                   import seconddependency.SecondDependency;
                   class Main {
                       public static void main(String[] args){
                           System.out.println(new FirstDependency());
                           System.out.println(new SecondDependency());
                       }
                   }
                   """);
        var dependentClasses = path.resolve("dependent-classes");

        dsl.givenDependency(firstDependencyClasses)
            .givenDependency(secondDependencyClasses)
            .givenConfiguration("sources.directory", dependentSources)
            .givenConfiguration("classes.directory", dependentClasses)
            .tasks()
            .execute();

        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }
}