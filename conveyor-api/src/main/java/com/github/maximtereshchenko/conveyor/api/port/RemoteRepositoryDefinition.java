package com.github.maximtereshchenko.conveyor.api.port;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public record RemoteRepositoryDefinition(
    String name,
    URL url,
    Optional<Boolean> enabled
) implements RepositoryDefinition {

    public RemoteRepositoryDefinition {
        Objects.requireNonNull(name);
        Objects.requireNonNull(url);
        enabled = Objects.requireNonNullElse(enabled, Optional.empty());
    }
}
