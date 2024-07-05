package com.github.maximtereshchenko.conveyor.files;

import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class DeleteTests {

    @ParameterizedTest
    @DirectoryEntriesSource
    void givenDirectory_whenDeleteFileVisitor_thenDirectoryIsDeleted(Path directory)
        throws IOException {
        Files.walkFileTree(directory, new Delete());

        assertThat(directory).doesNotExist();
    }

    @Test
    void givenFile_whenDeleteFileVisitor_thenFileIsDeleted(@TempDir Path path) throws IOException {
        var file = Files.createFile(path.resolve("file"));

        Files.walkFileTree(file, new Delete());

        assertThat(file).doesNotExist();
    }
}