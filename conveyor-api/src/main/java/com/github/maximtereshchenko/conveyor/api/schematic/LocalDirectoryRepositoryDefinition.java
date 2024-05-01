package com.github.maximtereshchenko.conveyor.api.schematic;

import java.nio.file.Path;
import java.util.Objects;

public record LocalDirectoryRepositoryDefinition(String name, Path path)
    implements RepositoryDefinition {

    public LocalDirectoryRepositoryDefinition {
        Objects.requireNonNull(name);
        Objects.requireNonNull(path);
    }
}
