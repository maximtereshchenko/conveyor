package com.github.maximtereshchenko.zip;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class ZipArchiveTests {

    private static final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());

    static Stream<Arguments> directories() throws IOException {
        return Stream.of(
                directory(),
                directory("file"),
                directory("first", "second"),
                directory("directory/file"),
                directory("directory/first", "directory/second"),
                directory("directory/nested", "root")
            )
            .map(directory ->
                arguments(directory, temporaryDirectory().resolve("archive"), temporaryDirectory())
            );
    }

    private static Path directory(String... entries) throws IOException {
        var directory = temporaryDirectory();
        for (var entry : entries) {
            var file = directory.resolve(entry);
            Files.createDirectories(file.getParent());
            Files.writeString(file, file.getFileName().toString());
        }
        return directory;
    }

    private static Path temporaryDirectory() {
        try {
            return Files.createTempDirectory(
                Files.createDirectories(fileSystem.getPath("/temp")),
                null
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("directories")
    void givenArchiveContainer_whenArchive_thenContentsAreEqual(
        Path archiveContainer,
        Path archive,
        Path extracted
    ) {
        new ArchiveContainer(archiveContainer).archive(archive);
        new ZipArchive(archive).extract(extracted);

        assertThat(files(archiveContainer))
            .zipSatisfy(
                files(extracted),
                (actualFile, expectedFile) -> {
                    assertThat(archiveContainer.relativize(actualFile))
                        .isEqualTo(extracted.relativize(expectedFile));
                    assertThat(actualFile).hasSameTextualContentAs(expectedFile);
                }
            );
    }

    private List<Path> files(Path path) {
        if (Files.isRegularFile(path)) {
            return List.of(path);
        }
        try (var stream = Files.list(path)) {
            return stream.map(this::files)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}