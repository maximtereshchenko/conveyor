package com.github.maximtereshchenko.conveyor.api.port;

import java.util.Collection;

public record StoredArtifactDefinition(String name, int version, Collection<StoredDependencyDefinition> dependencies)
    implements ArtifactDefinition {}
