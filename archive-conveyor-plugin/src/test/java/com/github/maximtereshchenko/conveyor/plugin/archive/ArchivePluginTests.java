package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import com.github.maximtereshchenko.conveyor.plugin.test.PublishedArtifact;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class ArchivePluginTests {

    @Test
    void givenPlugin_whenTasks_thenTaskBindToArchiveRun(@TempDir Path path) throws IOException {
        var classes = path.resolve("classes");
        var destination = path.resolve("destination");

        new Dsl(new ArchivePlugin(), path)
            .givenConfiguration("classes.directory", classes)
            .givenConfiguration("destination", destination)
            .tasks()
            .contain(
                new ConveyorTask(
                    "archive",
                    Stage.ARCHIVE,
                    Step.RUN,
                    null,
                    Set.of(new PathConveyorTaskInput(classes)),
                    Set.of(new PathConveyorTaskOutput(destination)),
                    Cache.ENABLED
                ),
                new ConveyorTask(
                    "publish-jar-artifact",
                    Stage.ARCHIVE,
                    Step.FINALIZE,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                )
            );
    }

    @Test
    void givenNoClasses_whenExecuteTask_thenNoArtifact(@TempDir Path path) throws IOException {
        var archive = path.resolve("archive");

        new Dsl(new ArchivePlugin(), path)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("destination", archive)
            .tasks()
            .execute()
            .thenNoArtifactPublished();

        assertThat(archive).doesNotExist();
    }

    @ParameterizedTest
    @DirectoryEntriesSource
    void givenClasses_whenExecuteTask_thenArtifactContainsFiles(
        Path directory,
        @TempDir Path path
    ) throws IOException {
        var archive = path.resolve("archive");

        new Dsl(new ArchivePlugin(), path)
            .givenConfiguration("classes.directory", directory)
            .givenConfiguration("destination", archive)
            .tasks()
            .execute()
            .thenArtifactsPublished(
                new PublishedArtifact(
                    Convention.CONSTRUCTION_REPOSITORY_NAME,
                    archive,
                    ArtifactClassifier.CLASSES
                )
            );

        var extracted = path.resolve("extracted");
        new ZipArchive(archive).extract(extracted);
        assertThat(extracted).directoryContentIsEqualTo(directory);
    }
}
