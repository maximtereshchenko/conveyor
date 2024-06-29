package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
                    new TreeSet<>(Set.of(sources)),
                    new TreeSet<>(Set.of(classes)),
                    Cache.ENABLED
                ),
                new ConveyorTask(
                    "publish-exploded-jar-artifact",
                    Stage.COMPILE,
                    Step.FINALIZE,
                    null,
                    new TreeSet<>(),
                    new TreeSet<>(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "compile-test-sources",
                    Stage.TEST,
                    Step.PREPARE,
                    null,
                    new TreeSet<>(Set.of(classes, testSources)),
                    new TreeSet<>(Set.of(testClasses)),
                    Cache.ENABLED
                )
            );
    }
}
