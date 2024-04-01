package com.github.maximtereshchenko.conveyor.domain;

record StoredDependencyDefinition(String name, int version) implements ArtifactDefinition {}
