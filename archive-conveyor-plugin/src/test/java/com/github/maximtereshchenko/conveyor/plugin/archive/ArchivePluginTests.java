package com.github.maximtereshchenko.conveyor.plugin.archive;

import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import com.github.maximtereshchenko.conveyor.plugin.api.*;
import com.github.maximtereshchenko.conveyor.plugin.test.Dsl;
import com.github.maximtereshchenko.conveyor.plugin.test.PublishedArtifact;
import com.github.maximtereshchenko.conveyor.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class ArchivePluginTests {

    @Test
    void givenPlugin_whenTasks_thenTaskBindToArchiveRun(@TempDir Path path) throws IOException {
        new Dsl(new ArchivePlugin(), path)
            .givenConfiguration("classes.directory", path.resolve("classes"))
            .givenConfiguration("destination", path.resolve("destination"))
            .tasks()
            .contain(
                new ConveyorTask(
                    "archive",
                    BindingStage.ARCHIVE,
                    BindingStep.RUN,
                    null,
                    Set.of(),
                    Set.of(),
                    Cache.DISABLED
                ),
                new ConveyorTask(
                    "publish-jar-artifact",
                    BindingStage.ARCHIVE,
                    BindingStep.FINALIZE,
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

    @Test
    void givenNoConfiguration_whenExecuteTask_thenArtifactInDefaultLocationContainsFilesFromDefaultDirectory(
        @TempDir Path path
    ) throws IOException {
        var conveyor = path.resolve(".conveyor");
        var classes = Files.createDirectories(conveyor.resolve("classes"));
        Files.createFile(classes.resolve("Dummy.class"));

        new Dsl(new ArchivePlugin(), path)
            .tasks()
            .execute();

        var extracted = path.resolve("extracted");
        new ZipArchive(conveyor.resolve("project-1.0.0.jar")).extract(extracted);
        assertThat(extracted).directoryContentIsEqualTo(classes);
    }
}
