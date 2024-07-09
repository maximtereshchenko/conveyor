package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.Cache;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import com.github.maximtereshchenko.conveyor.plugin.test.PublishedArtifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class PublishPluginTests {

    @Test
    void givenPlugin_whenTasks_thenPublishArtifactBindingReturned(@TempDir Path path)
        throws IOException {
        var artifact = path.resolve("artifact");

        new Dsl(new PublishPlugin(), path)
            .givenConfiguration("artifact.location", artifact)
            .tasks()
            .contain(
                new ConveyorTask(
                    "publish-artifact",
                    Stage.PUBLISH,
                    Step.RUN,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                )
            );
    }

    @Test
    void givenArtifactExists_whenExecuteTask_thenArtifactPublished(@TempDir Path path)
        throws IOException {
        var artifact = Files.createFile(path.resolve("artifact"));

        new Dsl(new PublishPlugin(), path)
            .givenConfiguration("artifact.location", artifact)
            .givenConfiguration("repository", "target")
            .tasks()
            .execute()
            .thenArtifactsPublished(
                new PublishedArtifact("target", artifact, ArtifactClassifier.CLASSES),
                new PublishedArtifact(
                    "target",
                    path.resolve("conveyor.json"),
                    ArtifactClassifier.SCHEMATIC_DEFINITION
                )
            );
    }

    @Test
    void givenNoArtifact_whenExecuteTask_thenNoArtifactPublished(@TempDir Path path)
        throws IOException {
        var artifact = path.resolve("artifact");

        new Dsl(new PublishPlugin(), path)
            .givenConfiguration("artifact.location", artifact)
            .givenConfiguration("repository", "target")
            .tasks()
            .execute()
            .thenArtifactsPublished(
                new PublishedArtifact(
                    "target",
                    path.resolve("conveyor.json"),
                    ArtifactClassifier.SCHEMATIC_DEFINITION
                )
            );
    }

    @Test
    void givenConveyorSchematic_whenExecuteTask_thenSchematicDefinitionPublished(
        @TempDir Path path
    ) throws IOException {
        new Dsl(new PublishPlugin(), path)
            .givenConfiguration("artifact.location", path.resolve("artifact"))
            .givenConfiguration("repository", "target")
            .tasks()
            .execute()
            .thenArtifactsPublished(
                new PublishedArtifact(
                    "target",
                    path.resolve("conveyor.json"),
                    ArtifactClassifier.SCHEMATIC_DEFINITION
                )
            );
    }
}
