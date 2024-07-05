package com.github.maximtereshchenko.conveyor.files;

import com.github.maximtereshchenko.conveyor.common.test.DirectoryEntriesSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

final class CopyTests {

    @ParameterizedTest
    @DirectoryEntriesSource
    void givenDirectory_whenCopyFileVisitor_thenDirectoryIsCopied(
        Path directory,
        @TempDir Path path
    ) throws IOException {
        var copy = path.resolve("copy");

        Files.walkFileTree(directory, new Copy(directory, copy));

        assertThat(copy).directoryContentIsEqualTo(directory);
    }

    @Test
    void givenFile_whenCopyFileVisitor_thenFileIsCopied(@TempDir Path path) throws IOException {
        var source = Files.writeString(path.resolve("source"), "content");
        var copy = path.resolve("copy");

        Files.walkFileTree(source, new Copy(source, copy));

        assertThat(copy).hasSameTextualContentAs(source);
    }
}