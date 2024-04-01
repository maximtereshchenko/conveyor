package com.github.maximtereshchenko.conveyor.api;

import java.nio.file.Path;

public record BuildSucceeded(Path projectDefinition, String name, int version) implements BuildResult {}
