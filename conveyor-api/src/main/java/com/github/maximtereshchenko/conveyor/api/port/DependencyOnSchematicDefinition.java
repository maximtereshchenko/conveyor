package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Optional;

public record DependencyOnSchematicDefinition(String schematic, Optional<DependencyScope> scope)
    implements SchematicDependencyDefinition {}
