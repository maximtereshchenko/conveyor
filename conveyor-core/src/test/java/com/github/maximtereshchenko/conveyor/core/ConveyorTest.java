package com.github.maximtereshchenko.conveyor.core;

import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@ExtendWith(ConveyorExtension.class)
abstract class ConveyorTest {

    Path defaultCacheDirectory(Path path) {
        return path.resolve(".conveyor-cache");
    }

    Path conveyorJson(Path path) {
        return path.resolve("conveyor.json");
    }

    Instant instant(Path path) throws IOException {
        return Instant.parse(Files.readString(path));
    }
}
