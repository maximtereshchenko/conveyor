package com.github.maximtereshchenko.zip;

import com.github.maximtereshchenko.test.common.Directories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.maximtereshchenko.test.common.Directories.temporaryDirectory;
import static org.assertj.core.api.Assertions.assertThatCode;

final class ZipArchiveTests {

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenArchiveContainer_whenArchive_thenContentsAreEqual(
        Set<String> entries,
        @TempDir Path path
    ) throws IOException {
        var archiveContainer = Directories.writeFiles(
            Directories.temporaryDirectory(path),
            entries
        );
        var archive = temporaryDirectory(path).resolve("archive");
        var extracted = temporaryDirectory(path);

        new ZipArchiveContainer(archiveContainer).archive(archive);
        new ZipArchive(archive).extract(extracted);

        Directories.assertThatDirectoryContentsEqual(archiveContainer, extracted);
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