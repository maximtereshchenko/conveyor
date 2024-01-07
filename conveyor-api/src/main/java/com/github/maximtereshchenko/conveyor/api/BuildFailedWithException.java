package com.github.maximtereshchenko.conveyor.api;

public record BuildFailedWithException(Exception exception) implements BuildResult {}
