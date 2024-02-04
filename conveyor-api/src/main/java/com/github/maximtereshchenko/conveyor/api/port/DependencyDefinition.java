package com.github.maximtereshchenko.conveyor.api.port;

public sealed interface DependencyDefinition permits ExternalDependencyDefinition, LocalProjectDependencyDefinition {}
