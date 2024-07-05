package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class CompilePluginTests {

    private final ConveyorPlugin plugin = new CompilePlugin();

    @Test
    void givenPlugin_whenTasks_thenTaskBindToCompileRunFinalizeTestPrepare(@TempDir Path path)
        throws IOException {
        var sources = path.resolve("sources");
        var classes = path.resolve("classes");
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var implementationDependency = path.resolve("implementation");
        var testDependency = path.resolve("test");

        assertThat(
            plugin.tasks(
                FakeConveyorSchematic.from(
                    path,
                    Map.of(
                        implementationDependency, DependencyScope.IMPLEMENTATION,
                        testDependency, DependencyScope.TEST
                    )
                ),
                Map.of(
                    "sources.directory", sources.toString(),
                    "classes.directory", classes.toString(),
                    "test.sources.directory", testSources.toString(),
                    "test.classes.directory", testClasses.toString()
                )
            )
        )
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(
                new ConveyorTask(
                    "compile-sources",
                    Stage.COMPILE,
                    Step.RUN,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(sources),
                        new PathConveyorTaskInput(implementationDependency)
                    ),
                    Set.of(new PathConveyorTaskOutput(classes)),
                    Cache.ENABLED
                ),
                new ConveyorTask(
                    "publish-exploded-jar-artifact",
                    Stage.COMPILE,
                    Step.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "compile-test-sources",
                    Stage.TEST,
                    Step.PREPARE,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(classes),
                        new PathConveyorTaskInput(testSources),
                        new PathConveyorTaskInput(implementationDependency),
                        new PathConveyorTaskInput(testDependency)
                    ),
                    Set.of(new PathConveyorTaskOutput(testClasses)),
                    Cache.ENABLED
                )
            );
    }
}
