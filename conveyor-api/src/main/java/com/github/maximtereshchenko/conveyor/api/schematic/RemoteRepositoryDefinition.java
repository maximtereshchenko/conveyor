package com.github.maximtereshchenko.conveyor.api.schematic;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public record RemoteRepositoryDefinition(
    String name,
    URI uri,
    Optional<Boolean> enabled
) implements RepositoryDefinition {

    public RemoteRepositoryDefinition {
        Objects.requireNonNull(name);
        Objects.requireNonNull(uri);
        enabled = Objects.requireNonNullElse(enabled, Optional.empty());
    }
}
