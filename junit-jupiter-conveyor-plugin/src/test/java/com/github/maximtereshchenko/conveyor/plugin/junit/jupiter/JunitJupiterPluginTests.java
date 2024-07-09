package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.files.FileTree;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskInput;
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
            .givenDependency(implementationDependency, DependencyScope.IMPLEMENTATION)
            .givenDependency(testDependency, DependencyScope.TEST)
            .givenConfiguration("test.classes.directory", testClasses)
            .givenConfiguration("classes.directory", classes)
            .tasks()
            .contain(
                new ConveyorTask(
                    "execute-junit-jupiter-tests",
                    Stage.TEST,
                    Step.RUN,
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
        compiler.compile(Set.of(testSourceClass), classpath, testClasses);

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
