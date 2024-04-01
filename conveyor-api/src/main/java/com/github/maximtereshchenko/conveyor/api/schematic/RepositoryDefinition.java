package com.github.maximtereshchenko.conveyor.api.schematic;

import java.util.Optional;

public sealed interface RepositoryDefinition
    permits LocalDirectoryRepositoryDefinition, RemoteRepositoryDefinition {

    String name();

    Optional<Boolean> enabled();
}
