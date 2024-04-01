package com.github.maximtereshchenko.conveyor.domain.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;

@ExtendWith(ConveyorExtension.class)
abstract class ConveyorTest {

    Path defaultBuildDirectory(Path path) {
        return path.resolve(".conveyor");
    }

    Collection<Path> modulePath(Path path) {
        try {
            return Files.readAllLines(path)
                .stream()
                .map(Paths::get)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Instant instant(Path path) {
        try {
            return Instant.parse(Files.readString(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
