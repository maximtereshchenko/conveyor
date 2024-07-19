package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.plugin.api.*;
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
            .givenDependency(implementationDependency, ClasspathScope.IMPLEMENTATION)
            .givenDependency(testDependency, ClasspathScope.TEST)
            .givenConfiguration("sources.directory", sources)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("test.sources.directory", testSources)
            .givenConfiguration("test.classes.directory", testClasses)
            .tasks()
            .contain(
                new ConveyorTask(
                    "compile-sources",
                    BindingStage.COMPILE,
                    BindingStep.RUN,
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
                    BindingStage.COMPILE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "compile-test-sources",
                    BindingStage.TEST,
                    BindingStep.PREPARE,
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

    @Test
    void givenNoConfiguration_whenTasks_thenTasksHaveDefaultInputs(@TempDir Path path)
        throws IOException {
        var classes = path.resolve(".conveyor").resolve("classes");

        new Dsl(new CompilePlugin(), path)
            .tasks()
            .contain(
                new ConveyorTask(
                    "compile-sources",
                    BindingStage.COMPILE,
                    BindingStep.RUN,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(
                            path.resolve("src").resolve("main").resolve("java")
                        )
                    ),
                    Set.of(new PathConveyorTaskOutput(classes)),
                    Cache.ENABLED
                ),
                new ConveyorTask(
                    "publish-exploded-jar-artifact",
                    BindingStage.COMPILE,
                    BindingStep.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "compile-test-sources",
                    BindingStage.TEST,
                    BindingStep.PREPARE,
                    null,
                    Set.of(
                        new PathConveyorTaskInput(classes),
                        new PathConveyorTaskInput(
                            path.resolve("src").resolve("test").resolve("java")
                        )
                    ),
                    Set.of(
                        new PathConveyorTaskOutput(
                            path.resolve(".conveyor").resolve("test-classes")
                        )
                    ),
                    Cache.ENABLED
                )
            );
    }
}
