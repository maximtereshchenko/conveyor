package com.github.maximtereshchenko.conveyor.api.schematic;

public sealed interface RepositoryDefinition
    permits LocalDirectoryRepositoryDefinition, RemoteRepositoryDefinition {

    String name();
}
