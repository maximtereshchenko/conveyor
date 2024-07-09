package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskInput;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskOutput;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

final class CompilePluginTests {

    @Test
    void givenPlugin_whenTasks_thenTaskBindToCompileRunFinalizeTestPrepare(@TempDir Path path)
        throws IOException {
        var sources = path.resolve("sources");
        var classes = path.resolve("classes");
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");
        var implementationDependency = path.resolve("implementation");
        var testDependency = path.resolve("test");

        new Dsl(new CompilePlugin(), path)
            .givenDependency(implementationDependency, DependencyScope.IMPLEMENTATION)
            .givenDependency(testDependency, DependencyScope.TEST)
            .givenConfiguration("sources.directory", sources)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("test.sources.directory", testSources)
            .givenConfiguration("test.classes.directory", testClasses)
            .tasks()
            .contain(
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
