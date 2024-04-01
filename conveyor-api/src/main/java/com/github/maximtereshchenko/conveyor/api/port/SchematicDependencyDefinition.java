package com.github.maximtereshchenko.conveyor.api.port;

public sealed interface SchematicDependencyDefinition permits DependencyOnArtifactDefinition,
    DependencyOnSchematicDefinition {}
