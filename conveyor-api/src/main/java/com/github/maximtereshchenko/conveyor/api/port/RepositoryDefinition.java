package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;
import java.util.Optional;

public record RepositoryDefinition(String name, Path path, Optional<Boolean> enabled) {}
