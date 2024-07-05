package com.github.maximtereshchenko.conveyor.zip;

import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;
import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThatCode;

final class ZipArchiveTests {

    @ParameterizedTest
    @DirectoryEntriesSource
    void givenArchiveContainer_whenArchive_thenContentsAreEqual(
        Path directory,
        @TempDir Path path
    ) throws IOException {
        var archive = path.resolve("archive");
        var extracted = path.resolve("extracted");

        new ZipArchiveContainer(directory).archive(archive);
        new ZipArchive(archive).extract(extracted);

        assertThat(extracted).directoryContentIsEqualTo(directory);
    }

    @Test
    void givenArchiveContainsDirectoryEntry_whenExtract_thenDirectoryCreated(@TempDir Path path)
        throws IOException {
        var archive = path.resolve("archive");
        try (var zipOutputStream = new ZipOutputStream(Files.newOutputStream(archive))) {
            zipOutputStream.putNextEntry(new ZipEntry("directory/"));
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("directory/file"));
            zipOutputStream.write("content".getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }
        var destination = Files.createDirectory(path.resolve("destination"));
        var zipArchive = new ZipArchive(archive);

        assertThatCode(() -> zipArchive.extract(destination)).doesNotThrowAnyException();
    }
}