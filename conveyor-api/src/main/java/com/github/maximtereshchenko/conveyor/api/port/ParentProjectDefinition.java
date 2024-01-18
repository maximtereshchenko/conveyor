package com.github.maximtereshchenko.conveyor.api.port;

public record ParentProjectDefinition(String name, int version) implements ParentDefinition, ArtifactDefinition {}
