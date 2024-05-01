package com.github.maximtereshchenko.conveyor.api.schematic;

import java.net.URI;
import java.util.Objects;

public record RemoteRepositoryDefinition(String name, URI uri) implements RepositoryDefinition {

    public RemoteRepositoryDefinition {
        Objects.requireNonNull(name);
        Objects.requireNonNull(uri);
    }
}
