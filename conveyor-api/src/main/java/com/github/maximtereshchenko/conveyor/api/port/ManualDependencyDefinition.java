package com.github.maximtereshchenko.conveyor.api.port;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

public record ManualDependencyDefinition(String name, String version, DependencyScope scope) {}
