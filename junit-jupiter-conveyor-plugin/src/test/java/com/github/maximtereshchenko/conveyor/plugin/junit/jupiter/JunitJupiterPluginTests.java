package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class JunitJupiterPluginTests {

    private final ConveyorPlugin plugin = new JunitJupiterPlugin();
    private final Compiler compiler = new Compiler();

    @Test
    void givenPlugin_whenTasks_thenRunJunitJupiterTestsBindingReturned(@TempDir Path path)
        throws IOException {
        var testClasses = path.resolve("test-classes");
        var classes = path.resolve("classes");

        assertThat(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
                Map.of(
                    "test.classes.directory", testClasses.toString(),
                    "classes.directory", classes.toString()
                )
            )
        )
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(
                new ConveyorTask(
                    "execute-junit-jupiter-tests",
                    Stage.TEST,
                    Step.RUN,
                    null,
                    Set.of(testClasses, classes),
                    Set.of(),
                    Cache.ENABLED
                )
            );
    }

    @Test
    void givenNoTestClasses_whenExecuteTasks_thenNoTestsAreExecuted(@TempDir Path path)
        throws IOException {
        var tasks = plugin.tasks(
            FakeConveyorSchematic.from(path),
            Map.of(
                "test.classes.directory", path.resolve("testClasses").toString(),
                "classes.directory", path.resolve("classes").toString()
            )
        );

        assertThatCode(() -> ConveyorTasks.executeTasks(tasks)).doesNotThrowAnyException();
    }

    @Test
    void givenNoClasses_whenExecuteTasks_thenTestsRunSuccessfully(@TempDir Path path)
        throws IOException {
        var testSources = path.resolve("testSources");
        var testClasses = path.resolve("testClasses");
        var classPath = classpath();
        compiler.compile(
            Set.of(
                Files.writeString(
                    Directories.createDirectoriesForFile(
                        testSources.resolve("test").resolve("MyTest.java")
                    ),
                    """
                    package test;
                    import org.junit.jupiter.api.Test;
                    import static org.junit.jupiter.api.Assertions.assertTrue;
                    final class MyTest {
                        @Test
                        void test() {
                            assertTrue(true);
                        }
                    }
                    """
                )
            ),
            classPath,
            testClasses
        );
        var tasks = plugin.tasks(
            FakeConveyorSchematic.from(path, classPath),
            Map.of(
                "test.classes.directory", testClasses.toString(),
                "classes.directory", path.resolve("classes").toString()
            )
        );

        assertThatCode(() -> ConveyorTasks.executeTasks(tasks)).doesNotThrowAnyException();
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
