package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;

public record RepositoryDefinition(String name, Path path, boolean enabled) {}
