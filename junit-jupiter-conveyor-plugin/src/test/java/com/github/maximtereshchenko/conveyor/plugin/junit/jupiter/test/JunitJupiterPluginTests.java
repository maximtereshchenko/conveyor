package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter.test;

import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTaskBindings;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematicBuilder;
import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@ExtendWith(JimfsExtension.class)
final class JunitJupiterPluginTests {

    private final Compiler compiler = new Compiler();

    @Test
    void givenPlugin_whenBindings_thenRunJunitJupiterTestsBindingReturned(Path path) {
        ConveyorTaskBindings.from(FakeConveyorSchematicBuilder.discoveryDirectory(path).build())
            .assertThat()
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .contains(tuple(Stage.TEST, Step.RUN));
    }

    @Test
    void givenTests_whenExecuteTasks_thenTestsExecuted(Path path) throws IOException {
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var modulePath = Stream.of(System.getProperty("jdk.module.path").split(":"))
            .map(path.getFileSystem()::getPath)
            .collect(Collectors.toSet());
        compiler.compile(
            Set.of(
                Files.writeString(
                    Directories.createDirectoriesForFile(testSources.resolve("module-info.java")),
                    """
                    module test {
                        requires org.junit.jupiter.api;
                        opens test to org.junit.platform.commons;
                    }
                    """
                ),
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
            Stream.of(System.getProperty("jdk.module.path").split(":"))
                .map(Paths::get)
                .collect(Collectors.toSet()),
            testClasses
        );
        var schematic = modulePath.stream()
            .reduce(
                FakeConveyorSchematicBuilder.discoveryDirectory(path),
                FakeConveyorSchematicBuilder::dependency,
                (a, b) -> a
            )
            .build();
        var outputStream = new ByteArrayOutputStream();
        var standardOut = System.out;
        System.setOut(new PrintStream(outputStream));

        ConveyorTaskBindings.from(schematic)
            .executeTasks(
                schematic.product(path, ProductType.EXPLODED_MODULE),
                schematic.product(testClasses, ProductType.EXPLODED_TEST_MODULE)
            );

        System.setOut(standardOut);
        assertThat(outputStream.toString().trim()).isEqualTo("test() - OK");
    }

    @Test
    void givenFailingTest_whenExecuteTasks_thenExceptionReported(Path path) throws IOException {
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var modulePath = Stream.of(System.getProperty("jdk.module.path").split(":"))
            .map(path.getFileSystem()::getPath)
            .collect(Collectors.toSet());
        compiler.compile(
            Set.of(
                Files.writeString(
                    Directories.createDirectoriesForFile(testSources.resolve("module-info.java")),
                    """
                    module test {
                        requires org.junit.jupiter.api;
                        opens test to org.junit.platform.commons;
                    }
                    """
                ),
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
            Stream.of(System.getProperty("jdk.module.path").split(":"))
                .map(Paths::get)
                .collect(Collectors.toSet()),
            testClasses
        );
        var schematic = modulePath.stream()
            .reduce(
                FakeConveyorSchematicBuilder.discoveryDirectory(path),
                FakeConveyorSchematicBuilder::dependency,
                (a, b) -> a
            )
            .build();
        var bindings = ConveyorTaskBindings.from(schematic);
        var explodedModule = schematic.product(path, ProductType.EXPLODED_MODULE);
        var explodedTestModule = schematic.product(testClasses, ProductType.EXPLODED_TEST_MODULE);
        var outputStream = new ByteArrayOutputStream();
        var standardOut = System.out;
        System.setOut(new PrintStream(outputStream));

        assertThatThrownBy(() -> bindings.executeTasks(explodedModule, explodedTestModule))
            .isInstanceOf(IllegalArgumentException.class);

        System.setOut(standardOut);
        assertThat(outputStream)
            .hasToString("""
                         test() - FAILED
                         org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
                           at test/test.MyTest.test(MyTest.java:7)
                         """);
    }
}
