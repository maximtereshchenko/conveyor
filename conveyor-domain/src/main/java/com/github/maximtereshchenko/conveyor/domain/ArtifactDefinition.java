package com.github.maximtereshchenko.conveyor.domain;

import java.util.Collection;

record ArtifactDefinition(String name, int version, Collection<DependencyDefinition> dependencies)
    implements VersionedArtifactDefinition {}
