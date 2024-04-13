package com.github.maximtereshchenko.zip;

import com.github.maximtereshchenko.test.common.Directories;
import com.github.maximtereshchenko.test.common.JimfsExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.maximtereshchenko.test.common.Directories.temporaryDirectory;

@ExtendWith(JimfsExtension.class)
final class ZipArchiveTests {

    static Stream<Arguments> entries() {
        return Directories.differentDirectoryEntries()
            .map(Arguments::arguments);
    }

    @ParameterizedTest
    @MethodSource("entries")
    void givenArchiveContainer_whenArchive_thenContentsAreEqual(Set<String> entries, Path path)
        throws IOException {
        var archiveContainer = Directories.writeFiles(
            Directories.temporaryDirectory(path),
            entries
        );
        var archive = temporaryDirectory(path).resolve("archive");
        var extracted = temporaryDirectory(path);

        new ArchiveContainer(archiveContainer).archive(archive);
        new ZipArchive(archive).extract(extracted);

        Directories.assertThatDirectoryContentsEqual(archiveContainer, extracted);
    }
}