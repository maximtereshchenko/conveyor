package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

public record ManualDependencyDefinition(String name, int version, DependencyScope scope) {}
