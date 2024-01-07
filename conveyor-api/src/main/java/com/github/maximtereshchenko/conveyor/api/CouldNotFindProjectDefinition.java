package com.github.maximtereshchenko.conveyor.api;

import java.nio.file.Path;

public record CouldNotFindProjectDefinition(Path path) implements BuildResult {}
