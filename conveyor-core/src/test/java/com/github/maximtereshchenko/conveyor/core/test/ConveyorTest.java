package com.github.maximtereshchenko.conveyor.core.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@ExtendWith(ConveyorExtension.class)
abstract class ConveyorTest {

    Path defaultConstructionDirectory(Path path) {
        return path.resolve(".conveyor");
    }

    Path defaultCacheDirectory(Path path) {
        return path.resolve(".conveyor-modules");
    }

    Path conveyorJson(Path path) {
        return path.resolve("conveyor.json");
    }

    Instant instant(Path path) throws IOException {
        return Instant.parse(Files.readString(path));
    }
}
