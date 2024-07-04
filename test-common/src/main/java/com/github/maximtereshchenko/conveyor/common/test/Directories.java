package com.github.maximtereshchenko.conveyor.common.test;

import com.github.maximtereshchenko.conveyor.files.FileTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public final class Directories {

    public static Stream<Set<String>> differentDirectoryEntries() {
        return Stream.of(
            Set.of(),
            Set.of("file"),
            Set.of("first", "second"),
            Set.of("directory/file"),
            Set.of("directory/first", "directory/second"),
            Set.of("directory/nested", "root")
        );
    }

    public static Path temporaryDirectory(Path path) throws IOException {
        return Files.createTempDirectory(Files.createDirectories(path), null);
    }

    public static Path writeFiles(Path directory, Set<String> entries) throws IOException {
        for (var entry : entries) {
            var file = directory.resolve(entry);
            Files.writeString(createDirectoriesForFile(file), file.getFileName().toString());
        }
        return Files.createDirectories(directory);
    }

    public static void assertThatDirectoryContentsEqual(
        Path actual,
        Path expected,
        Path... exclusions
    ) {
        assertThat(
            new FileTree(actual)
                .files()
                .stream()
                .filter(path -> !List.of(exclusions).contains(path))
        )
            .zipSatisfy(
                new FileTree(expected).files(),
                (actualFile, expectedFile) -> {
                    assertThat(actual.relativize(actualFile))
                        .isEqualTo(expected.relativize(expectedFile));
                    assertThat(actualFile).hasSameBinaryContentAs(expectedFile);
                }
            );
    }

    public static Path createDirectoriesForFile(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return path;
    }
}
