package com.github.maximtereshchenko.conveyor.plugin.publish;

import com.github.maximtereshchenko.conveyor.plugin.api.*;
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
                    "publish-schematic-definition",
                    BindingStage.PUBLISH,
                    BindingStep.RUN,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "publish-artifact",
                    BindingStage.PUBLISH,
                    BindingStep.RUN,
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
                new PublishedArtifact(
                    "target",
                    path.resolve("conveyor.json"),
                    ArtifactClassifier.SCHEMATIC_DEFINITION
                ),
                new PublishedArtifact("target", artifact, ArtifactClassifier.CLASSES)
            );
    }

    @Test
    void givenNoArtifactLocationConfiguration_whenExecuteTask_thenArtifactPublishedFromDefaultLocation(
        @TempDir Path path
    ) throws IOException {
        var jar = Files.createFile(
            Files.createDirectory(path.resolve(".conveyor")).resolve("project-1.0.0.jar")
        );

        new Dsl(new PublishPlugin(), path)
            .givenConfiguration("repository", "target")
            .tasks()
            .execute()
            .thenArtifactsPublished(
                new PublishedArtifact(
                    "target",
                    path.resolve("conveyor.json"),
                    ArtifactClassifier.SCHEMATIC_DEFINITION
                ),
                new PublishedArtifact("target", jar, ArtifactClassifier.CLASSES)
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
