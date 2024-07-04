package com.github.maximtereshchenko.conveyor.filevisitors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

final class GenericTests {

    @Test
    void givenFiles_whenFileVisitor_thenActionPerformedOnEachFile(@TempDir Path path)
        throws IOException {
        var first = Files.createFile(path.resolve("first"));
        var second = Files.createFile(
            Files.createDirectory(path.resolve("directory")).resolve("second")
        );
        var list = new ArrayList<>();

        Files.walkFileTree(path, new Generic(list::add));

        assertThat(list).containsOnly(first, second);
    }
}