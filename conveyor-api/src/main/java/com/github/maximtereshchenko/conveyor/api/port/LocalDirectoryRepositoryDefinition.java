package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public record LocalDirectoryRepositoryDefinition(String name, Path path, Optional<Boolean> enabled)
    implements RepositoryDefinition {

    public LocalDirectoryRepositoryDefinition {
        Objects.requireNonNull(name);
        Objects.requireNonNull(path);
        enabled = Objects.requireNonNullElse(enabled, Optional.empty());
    }
}
