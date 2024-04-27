package com.github.maximtereshchenko.compiler.test;

import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JimfsExtension.class)
final class CompilerTests {

    private final Compiler compiler = new Compiler();

    @Test
    void givenSources_whenExecuteTasks_thenSourcesAreCompiled(Path path) throws IOException {
        var sources = path.resolve("sources");
        var classes = path.resolve("classes");

        compiler.compile(
            Set.of(
                write(moduleInfoJava(sources), "module main {}"),
                write(
                    sources.resolve("main").resolve("Main.java"),
                    """
                    package main;
                    class Main {}
                    """
                )
            ),
            Set.of(),
            classes
        );

        assertThat(moduleInfoClass(classes)).exists();
        assertThat(classes.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenDependency_whenExecuteTasks_thenSourcesAreCompiledWithDependency(Path path)
        throws IOException {
        var dependencySources = path.resolve("dependency-sources");
        var dependencyClasses = path.resolve("dependency-classes");
        compiler.compile(
            Set.of(
                write(
                    moduleInfoJava(dependencySources),
                    """
                    module dependency {
                        exports dependency;
                    }
                    """
                ),
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
                    moduleInfoJava(dependentSources),
                    """
                    module main {
                        requires dependency;
                    }
                    """
                ),
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

        assertThat(moduleInfoClass(dependentClasses)).exists();
        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }

    @Test
    void givenMultipleDependencies_whenExecuteTasks_thenSourcesAreCompiledWithAllDependencies(
        Path path
    ) throws IOException {
        var firstDependencySources = path.resolve("first-dependency-sources");
        var firstDependencyClasses = path.resolve("first-dependency-classes");
        compiler.compile(
            Set.of(
                write(
                    moduleInfoJava(firstDependencySources),
                    """
                    module firstdependency {
                        exports firstdependency;
                    }
                    """
                ),
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
                    moduleInfoJava(secondDependencySources),
                    """
                    module seconddependency {
                        exports seconddependency;
                    }
                    """
                ),
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
                    moduleInfoJava(dependentSources),
                    """
                    module main {
                        requires firstdependency;
                        requires seconddependency;
                    }
                    """
                ),
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

        assertThat(moduleInfoClass(dependentClasses)).exists();
        assertThat(dependentClasses.resolve("main").resolve("Main.class")).exists();
    }

    Path moduleInfoJava(Path path) {
        return path.resolve("module-info.java");
    }

    Path moduleInfoClass(Path path) {
        return path.resolve("module-info.class");
    }

    Path write(Path path, String content) throws IOException {
        return Files.writeString(Directories.createDirectoriesForFile(path), content);
    }
}
