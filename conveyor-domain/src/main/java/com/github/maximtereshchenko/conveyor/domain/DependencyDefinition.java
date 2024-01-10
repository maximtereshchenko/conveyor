package com.github.maximtereshchenko.conveyor.domain;

record DependencyDefinition(String name, int version) implements VersionedArtifactDefinition {}
