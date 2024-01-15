package com.github.maximtereshchenko.conveyor.api.port;

public record StoredDependencyDefinition(String name, int version) implements ArtifactDefinition {}
