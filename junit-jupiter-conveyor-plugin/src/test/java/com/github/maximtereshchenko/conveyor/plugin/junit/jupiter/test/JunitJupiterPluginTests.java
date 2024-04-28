package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter.test;

import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.junit.jupiter.JunitJupiterPlugin;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import org.apiguardian.api.API;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;

final class JunitJupiterPluginTests {

    private final ConveyorPlugin plugin = new JunitJupiterPlugin();
    private final Compiler compiler = new Compiler();

    @Test
    void givenPlugin_whenBindings_thenRunJunitJupiterTestsBindingReturned(@TempDir Path path) {
        assertThat(
            plugin.bindings(
                FakeConveyorSchematicBuilder.discoveryDirectory(path).build(),
                Map.of()
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.TEST, Step.RUN));
    }

    @Test
    void givenTests_whenExecuteTasks_thenTestsExecuted(@TempDir Path path) throws IOException {
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var classPath = classPath();
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
        var schematic = classPath.stream()
            .reduce(
                FakeConveyorSchematicBuilder.discoveryDirectory(path),
                FakeConveyorSchematicBuilder::dependency,
                (a, b) -> a
            )
            .build();
        var outputStream = new ByteArrayOutputStream();
        var standardOut = System.out;
        System.setOut(new PrintStream(outputStream));

        ConveyorTasks.executeTasks(
            schematic,
            plugin,
            new Product(schematic.coordinates(), path, ProductType.EXPLODED_JAR),
            new Product(schematic.coordinates(), testClasses, ProductType.EXPLODED_TEST_JAR)
        );

        System.setOut(standardOut);
        assertThat(outputStream.toString().trim()).isEqualTo("test() - OK");
    }

    @Test
    void givenFailingTest_whenExecuteTasks_thenExceptionReported(@TempDir Path path)
        throws IOException {
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var classPath = classPath();
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
                            assertTrue(false);
                        }
                    }
                    """
                )
            ),
            classPath,
            testClasses
        );
        var schematic = classPath.stream()
            .reduce(
                FakeConveyorSchematicBuilder.discoveryDirectory(path),
                FakeConveyorSchematicBuilder::dependency,
                (a, b) -> a
            )
            .build();
        var explodedJar = new Product(
            schematic.coordinates(),
            path,
            ProductType.EXPLODED_JAR
        );
        var explodedTestJar = new Product(
            schematic.coordinates(),
            testClasses,
            ProductType.EXPLODED_TEST_JAR
        );
        var outputStream = new ByteArrayOutputStream();
        var standardOut = System.out;
        System.setOut(new PrintStream(outputStream));

        assertThatThrownBy(() ->
            ConveyorTasks.executeTasks(schematic, plugin, explodedJar, explodedTestJar)
        )
            .isInstanceOf(IllegalArgumentException.class);

        System.setOut(standardOut);
        assertThat(outputStream)
            .hasToString("""
                         test() - FAILED
                         org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
                           at test.MyTest.test(MyTest.java:7)
                         """);
    }

    @Test
    void givenNoExplodedTestJar_whenExecuteTasks_thenNoTestsAreExecuted(@TempDir Path path) {
        var schematic = FakeConveyorSchematicBuilder.discoveryDirectory(path).build();
        var explodedJar = new Product(
            schematic.coordinates(),
            path,
            ProductType.EXPLODED_JAR
        );

        assertThatCode(() -> ConveyorTasks.executeTasks(schematic, plugin, explodedJar))
            .doesNotThrowAnyException();
    }

    @Test
    void givenNoExplodedJar_whenExecuteTasks_thenNoTestsAreExecuted(@TempDir Path path)
        throws IOException {
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var classPath = classPath();
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
        var schematic = classPath.stream()
            .reduce(
                FakeConveyorSchematicBuilder.discoveryDirectory(path),
                FakeConveyorSchematicBuilder::dependency,
                (a, b) -> a
            )
            .build();
        var explodedTestJar = new Product(
            schematic.coordinates(),
            testClasses,
            ProductType.EXPLODED_TEST_JAR
        );

        assertThatCode(() -> ConveyorTasks.executeTasks(schematic, plugin, explodedTestJar))
            .doesNotThrowAnyException();
    }

    @Test
    void givenProductsFromOtherSchematics_whenExecuteTasks_thenTestsAreExecutedForCurrentSchematic(
        @TempDir Path path
    ) throws IOException {
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var classPath = classPath();
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
        var schematic = classPath.stream()
            .reduce(
                FakeConveyorSchematicBuilder.discoveryDirectory(path),
                FakeConveyorSchematicBuilder::dependency,
                (a, b) -> a
            )
            .build();
        var explodedTestJar = new Product(
            schematic.coordinates(),
            testClasses,
            ProductType.EXPLODED_TEST_JAR
        );
        var otherExplodedTestJar = new Product(
            new SchematicCoordinates(
                "group",
                "other-schematic",
                "1.0.0"
            ),
            path.resolve("other-exploded-test-jar"),
            ProductType.EXPLODED_TEST_JAR
        );

        assertThatCode(() ->
            ConveyorTasks.executeTasks(
                schematic,
                plugin,
                otherExplodedTestJar,
                explodedTestJar
            )
        )
            .doesNotThrowAnyException();
    }

    private Set<Path> classPath() {
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
