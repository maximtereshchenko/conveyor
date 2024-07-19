package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class JunitJupiterPluginTests {

    private final Compiler compiler = new Compiler();

    @Test
    void givenPlugin_whenTasks_thenRunJunitJupiterTestsBindingReturned(@TempDir Path path)
        throws IOException {
        var testClasses = path.resolve("test-classes");
        var classes = path.resolve("classes");
        var implementationDependency = path.resolve("implementation");
        var testDependency = path.resolve("test");

        new Dsl(new JunitJupiterPlugin(), path)
            .givenDependency(implementationDependency, ClasspathScope.IMPLEMENTATION)
            .givenDependency(testDependency, ClasspathScope.TEST)
            .givenConfiguration("test.classes.directory", testClasses)
            .givenConfiguration("classes.directory", classes)
            .tasks()
            .contain(
                new ConveyorTask(
                    "execute-junit-jupiter-tests",
                    BindingStage.TEST,
                    BindingStep.RUN,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(classes),
                        new PathConveyorTaskInput(testClasses),
                        new PathConveyorTaskInput(implementationDependency),
                        new PathConveyorTaskInput(testDependency)
                    ),
                    Set.of(),
                    Cache.ENABLED
                )
            );
    }

    @Test
    void givenNoConfiguration_whenTasks_thenTaskHasDefaultInputs(@TempDir Path path)
        throws IOException {
        var conveyor = path.resolve(".conveyor");

        new Dsl(new JunitJupiterPlugin(), path)
            .tasks()
            .contain(
                new ConveyorTask(
                    "execute-junit-jupiter-tests",
                    BindingStage.TEST,
                    BindingStep.RUN,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(conveyor.resolve("classes")),
                        new PathConveyorTaskInput(conveyor.resolve("test-classes"))
                    ),
                    Set.of(),
                    Cache.ENABLED
                )
            );
    }

    @Test
    void givenNoTestClasses_whenExecuteTasks_thenNoTestsAreExecuted(@TempDir Path path)
        throws IOException {
        new Dsl(new JunitJupiterPlugin(), path)
            .givenConfiguration("test.classes.directory", path.resolve("testClasses"))
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .tasks()
            .execute()
            .thenNoException();
    }

    @Test
    void givenNoClasses_whenExecuteTasks_thenTestsRunSuccessfully(@TempDir Path path)
        throws IOException {
        var testSources = path.resolve("testSources");
        var testClasses = path.resolve("testClasses");
        var classpath = classpath();
        var testSourceClass = testSources.resolve("test").resolve("MyTest.java");
        new FileTree(testSourceClass)
            .write("""
                   package test;
                   import org.junit.jupiter.api.Test;
                   import static org.junit.jupiter.api.Assertions.assertTrue;
                   final class MyTest {
                       @Test
                       void test() {
                           assertTrue(true);
                       }
                   }
                   """);
        compiler.compile(Set.of(testSourceClass), classpath, testClasses, System.err::println);

        new Dsl(new JunitJupiterPlugin(), path)
            .givenDependencies(classpath)
            .givenConfiguration("test.classes.directory", testClasses)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .tasks()
            .execute()
            .thenNoException();
    }

    private Set<Path> classpath() {
        return Stream.of(Test.class, API.class)
            .map(Class::getProtectionDomain)
            .map(ProtectionDomain::getCodeSource)
            .map(CodeSource::getLocation)
            .map(this::uri)
            .map(Paths::get)
            .collect(Collectors.toSet());
    }

    private URI uri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
