package com.github.maximtereshchenko.conveyor.api.port;

import java.net.URL;
import java.util.Optional;

public record RemoteRepositoryDefinition(
    String name,
    URL url,
    Optional<Boolean> enabled
) implements RepositoryDefinition {}
