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

final class DeleteTests {

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenDirectory_whenDeleteFileVisitor_thenDirectoryIsDeleted(
        Set<String> entries,
        @TempDir Path path
    ) throws IOException {
        Directories.writeFiles(path, entries);

        Files.walkFileTree(path, new Delete());

        assertThat(path).doesNotExist();
    }

    @Test
    void givenFile_whenCopyFileVisitor_thenFileIsCopied(@TempDir Path path) throws IOException {
        var file = Files.createFile(path.resolve("file"));

        Files.walkFileTree(file, new Delete());

        assertThat(file).doesNotExist();
    }
}