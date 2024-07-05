package com.github.maximtereshchenko.conveyor.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThat;
import static com.github.maximtereshchenko.conveyor.common.test.MoreAssertions.assertThatCode;

final class FileTreeTests {

    @Test
    void givenPath_whenWalk_thenFileVisitorUsed(@TempDir Path path) throws IOException {
        var file = Files.createFile(path.resolve("file"));
        var files = new ArrayList<>();

        new FileTree(path).walk(new Generic(files::add));

        assertThat(files).containsExactly(file);
    }

    @Test
    void givenPath_whenWrite_thenFileExists(@TempDir Path path) {
        var file = path.resolve("directory").resolve("file");

        new FileTree(file)
            .write(outputStream -> outputStream.write("content".getBytes(StandardCharsets.UTF_8)));

        assertThat(file).hasContent("content");
    }

    @Test
    void givenPath_whenRead_thenFileContentIsRead(@TempDir Path path) throws IOException {
        var bytes = "content".getBytes(StandardCharsets.UTF_8);
        var file = Files.write(path.resolve("file"), bytes);
        var actual = new byte[bytes.length];

        new FileTree(file).transfer(inputStream -> inputStream.read(actual));

        assertThat(actual).isEqualTo(bytes);
    }

    @Test
    void givenPath_whenRead_thenFileContentIsReadAsString(@TempDir Path path) throws IOException {
        var file = Files.writeString(path.resolve("file"), "content");

        assertThat(new FileTree(file).read()).isEqualTo("content");
    }

    @Test
    void givenPath_whenWrite_thenFileContainsNumber(@TempDir Path path) {
        var file = path.resolve("directory").resolve("file");

        new FileTree(file).write(123);

        assertThat(file).hasContent("123");
    }

    @Test
    void givenPath_whenWrite_thenFileContainsString(@TempDir Path path) {
        var file = path.resolve("directory").resolve("file");

        new FileTree(file).write("content");

        assertThat(file).hasContent("content");
    }

    @Test
    void givenExistingPath_whenDelete_thenFileIsDeleted(@TempDir Path path) throws IOException {
        var file = Files.createFile(path.resolve("file"));

        new FileTree(file).delete();

        assertThat(file).doesNotExist();
    }

    @Test
    void givenNonExistingPath_whenDelete_thenNoExceptionThrown(@TempDir Path path) {
        var fileTree = new FileTree(path.resolve("file"));

        assertThatCode(fileTree::delete).doesNotThrowAnyException();
    }
}