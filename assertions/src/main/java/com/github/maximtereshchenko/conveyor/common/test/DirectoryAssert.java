package com.github.maximtereshchenko.conveyor.common.test;

import org.assertj.core.api.PathAssert;
import org.assertj.core.api.SoftAssertions;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;

public final class DirectoryAssert extends PathAssert {

    DirectoryAssert(Path actual) {
        super(actual);
    }

    public DirectoryAssert directoryContentIsEqualTo(Path expected) throws IOException {
        return directoryContentIsEqualToIgnoring(expected);
    }

    public DirectoryAssert directoryContentIsEqualToIgnoring(Path expected, Path... exclusions)
        throws IOException {
        var actualFiles = files(actual, Set.of(exclusions));
        var expectedFiles = files(expected, Set.of());
        assertThat(actualFiles).containsOnlyKeys(expectedFiles.keySet());
        var assertions = new SoftAssertions();
        actualFiles.keySet()
            .forEach(relative ->
                assertions.assertThat(actualFiles.get(relative))
                    .hasSameBinaryContentAs(expectedFiles.get(relative))
            );
        assertions.assertAll();
        return this;
    }

    private Map<Path, Path> files(Path path, Set<Path> exclusions) throws IOException {
        if (!Files.exists(path)) {
            return Map.of();
        }
        var files = new TreeMap<Path, Path>();
        Files.walkFileTree(
            path,
            new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!exclusions.contains(file)) {
                        files.put(path.relativize(file), file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            }
        );
        return files;
    }
}
