package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskInput;
import com.github.maximtereshchenko.conveyor.plugin.api.PathConveyorTaskOutput;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

final class CompilePluginTests extends BaseTest {

    @Test
    void givenPlugin_whenTasks_thenTaskBindToCompileRunFinalizeTestPrepare(@TempDir Path path)
        throws IOException {
        var sources = path.resolve("sources");
        var classes = path.resolve("classes");
        var testSources = path.resolve("test-sources");
        var testClasses = path.resolve("test-classes");

        assertThat(
            plugin.tasks(
                FakeConveyorSchematic.from(path),
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
                    Set.of(new PathConveyorTaskInput(sources)),
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
                        new PathConveyorTaskInput(testSources)
                    ),
                    Set.of(new PathConveyorTaskOutput(testClasses)),
                    Cache.ENABLED
                )
            );
    }
}
