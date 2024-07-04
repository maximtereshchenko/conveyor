package com.github.maximtereshchenko.conveyor.filevisitors;

import com.github.maximtereshchenko.conveyor.common.test.Directories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class CopyTests {

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenDirectory_whenCopyFileVisitor_thenDirectoryIsCopied(
        Set<String> entries,
        @TempDir Path path
    ) throws IOException {
        var source = path.resolve("source");
        Directories.writeFiles(source, entries);
        var copy = path.resolve("copy");

        Files.walkFileTree(source, new Copy(source, copy));

        Directories.assertThatDirectoryContentsEqual(copy, source);
    }

    @Test
    void givenFile_whenCopyFileVisitor_thenFileIsCopied(@TempDir Path path) throws IOException {
        var source = Files.writeString(path.resolve("source"), "content");
        var copy = path.resolve("copy");

        Files.walkFileTree(source, new Copy(source, copy));

        assertThat(copy).hasSameTextualContentAs(source);
    }
}