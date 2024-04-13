package com.github.maximtereshchenko.test.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public final class Directories {

    private Directories() {
    }

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

    public static void assertThatDirectoryContentsEqual(Path actual, Path expected) {
        assertThat(files(actual))
            .zipSatisfy(
                files(expected),
                (actualFile, expectedFile) -> {
                    assertThat(actual.relativize(actualFile))
                        .isEqualTo(expected.relativize(expectedFile));
                    assertThat(actualFile).hasSameTextualContentAs(expectedFile);
                }
            );
    }

    public static Path createDirectoriesForFile(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return path;
    }

    private static List<Path> files(Path path) {
        if (Files.isRegularFile(path)) {
            return List.of(path);
        }
        try (var stream = Files.list(path)) {
            return stream.map(Directories::files)
                .flatMap(Collection::stream)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
