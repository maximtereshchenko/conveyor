package com.github.maximtereshchenko.conveyor.domain;

import java.util.Collection;

record StoredArtifactDefinition(String name, int version, Collection<StoredDependencyDefinition> dependencies)
    implements ArtifactDefinition {}
