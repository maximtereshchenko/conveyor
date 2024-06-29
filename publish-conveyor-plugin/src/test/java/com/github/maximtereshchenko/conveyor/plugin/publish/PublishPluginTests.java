package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.ConveyorTasks;
import com.github.maximtereshchenko.conveyor.plugin.test.FakeConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.test.PublishedArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

final class PublishPluginTests {

    private final ConveyorPlugin plugin = new PublishPlugin();

    @Test
    void givenPlugin_whenTasks_thenPublishArtifactBindingReturned(@TempDir Path path)
        throws IOException {
        var artifact = path.resolve("artifact");
        var schematic = FakeConveyorSchematic.from(path);

        assertThat(
            plugin.tasks(
                schematic,
                Map.of("artifact.location", artifact.toString())
            )
        )
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("action")
            .containsExactly(
                new ConveyorTask(
                    "publish-artifact",
                    Stage.PUBLISH,
                    Step.RUN,
                    null,
                    new TreeSet<>(Set.of(artifact, schematic.path())),
                    new TreeSet<>(),
                    Cache.DISABLED
                )
            );
    }

    @Test
    void givenArtifactExists_whenExecuteTask_thenArtifactPublished(@TempDir Path path)
        throws IOException {
        var artifact = Files.createFile(path.resolve("artifact"));
        var schematic = FakeConveyorSchematic.from(path);

        ConveyorTasks.executeTasks(
            plugin.tasks(
                schematic,
                Map.of(
                    "artifact.location", artifact.toString(),
                    "repository", "target"
                )
            )
        );

        assertThat(schematic.published())
            .contains(new PublishedArtifact("target", artifact, ArtifactClassifier.JAR));
    }

    @Test
    void givenNoArtifact_whenExecuteTask_thenNoArtifactPublished(@TempDir Path path)
        throws IOException {
        var artifact = path.resolve("artifact");
        var schematic = FakeConveyorSchematic.from(path);

        ConveyorTasks.executeTasks(
            plugin.tasks(
                schematic,
                Map.of(
                    "artifact.location", artifact.toString(),
                    "repository", "target"
                )
            )
        );

        assertThat(schematic.published())
            .doesNotContain(new PublishedArtifact("target", artifact, ArtifactClassifier.JAR));
    }

    @Test
    void givenConveyorSchematic_whenExecuteTask_thenSchematicDefinitionPublished(
        @TempDir Path path
    ) throws IOException {
        var schematic = FakeConveyorSchematic.from(path);

        ConveyorTasks.executeTasks(
            plugin.tasks(
                schematic,
                Map.of(
                    "artifact.location", path.resolve("artifact").toString(),
                    "repository", "target"
                )
            )
        );

        assertThat(schematic.published())
            .contains(
                new PublishedArtifact(
                    "target",
                    schematic.path(),
                    ArtifactClassifier.SCHEMATIC_DEFINITION
                )
            );
    }
}
