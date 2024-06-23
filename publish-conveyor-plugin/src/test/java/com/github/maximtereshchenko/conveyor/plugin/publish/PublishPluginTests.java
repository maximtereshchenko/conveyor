package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

final class PublishPluginTests {

    private final ConveyorPlugin plugin = new PublishPlugin();

    @Test
    void givenPlugin_whenBindings_thenPublishArtifactBindingReturned() {
        assertThat(
            plugin.bindings(
                new FakeConveyorSchematic(),
                Map.of("artifact.location", "")
            )
        )
            .extracting(ConveyorTaskBinding::stage, ConveyorTaskBinding::step)
            .containsExactly(tuple(Stage.PUBLISH, Step.RUN));
    }

    @Test
    void givenArtifactExists_whenExecuteTask_thenArtifactPublished(@TempDir Path path)
        throws IOException {
        var artifact = Files.createFile(path.resolve("artifact"));
        var schematic = new FakeConveyorSchematic(
            Files.createFile(path.resolve("conveyor.json")),
            Set.of()
        );

        ConveyorTasks.executeTasks(
            plugin.bindings(
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
        var schematic = new FakeConveyorSchematic(
            Files.createFile(path.resolve("conveyor.json")),
            Set.of()
        );

        ConveyorTasks.executeTasks(
            plugin.bindings(
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
        var schematic = new FakeConveyorSchematic(
            Files.createFile(path.resolve("conveyor.json")),
            Set.of()
        );

        ConveyorTasks.executeTasks(
            plugin.bindings(
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
