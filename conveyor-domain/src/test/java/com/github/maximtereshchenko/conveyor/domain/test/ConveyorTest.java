package com.github.maximtereshchenko.conveyor.domain.test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.jupiter.api.extension.ExtendWith;

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
}
