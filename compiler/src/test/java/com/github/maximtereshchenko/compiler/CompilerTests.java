package com.github.maximtereshchenko.compiler;

import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

final class CompilerTests {

    private final Compiler compiler = new Compiler();

    @Test
    void givenSources_whenExecuteTasks_thenSourcesAreCompiled(@TempDir Path path)
        throws IOException {
        var sources = path.resolve("sources");
        var classes = path.resolve("classes");

        compiler.compile(
            Set.of(
                write(
                    sources.resolve("main").resolve("Main.java"),
                    """
                    package main;
                    import java.util.*;
                    class Main {
                    public static void main(String[] args){
                    List list=new ArrayList();
                    System.out.println(list);
                    }
                    }
                    """
                )
            ),
            Set.of(),
            classes
        );

        assertThat(classes.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenDependency_whenExecuteTasks_thenSourcesAreCompiledWithDependency(@TempDir Path path)
        throws IOException {
        var dependencySources = path.resolve("dependency-sources");
        var dependencyClasses = path.resolve("dependency-classes");
        compiler.compile(
            Set.of(
                write(
                    dependencySources.resolve("dependency").resolve("Dependency.java"),
                    """
                    package dependency;
                    public class Dependency {}
                    """
                )
            ),
            Set.of(),
            dependencyClasses
        );
        var dependentSources = path.resolve("dependent-sources");
        var dependentClasses = path.resolve("dependent-classes");

        compiler.compile(
            Set.of(
                write(
                    dependentSources.resolve("main").resolve("Main.java"),
                    """
                    package main;
                    import dependency.Dependency;
                    class Main {
                        public static void main(String[] args){
                            System.out.println(new Dependency());
                        }
                    }
                    """
                )
            ),
            Set.of(dependencyClasses),
            dependentClasses
        );

        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenMultipleDependencies_whenExecuteTasks_thenSourcesAreCompiledWithAllDependencies(
        @TempDir Path path
    ) throws IOException {
        var firstDependencySources = path.resolve("first-dependency-sources");
        var firstDependencyClasses = path.resolve("first-dependency-classes");
        compiler.compile(
            Set.of(
                write(
                    firstDependencySources.resolve("firstdependency").resolve("FirstDependency" +
                                                                              ".java"),
                    """
                    package firstdependency;
                    public class FirstDependency {}
                    """
                )
            ),
            Set.of(),
            firstDependencyClasses
        );
        var secondDependencySources = path.resolve("second-dependency-sources");
        var secondDependencyClasses = path.resolve("second-dependency-classes");
        compiler.compile(
            Set.of(
                write(
                    secondDependencySources.resolve("seconddependency").resolve(
                        "SecondDependency.java"),
                    """
                    package seconddependency;
                    public class SecondDependency {}
                    """
                )
            ),
            Set.of(),
            secondDependencyClasses
        );
        var dependentSources = path.resolve("dependent-sources");
        var dependentClasses = path.resolve("dependent-classes");

        compiler.compile(
            Set.of(
                write(
                    dependentSources.resolve("main").resolve("Main.java"),
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
                )
            ),
            Set.of(firstDependencyClasses, secondDependencyClasses),
            dependentClasses
        );

        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }

    Path write(Path path, String content) throws IOException {
        return Files.writeString(Directories.createDirectoriesForFile(path), content);
    }
}
