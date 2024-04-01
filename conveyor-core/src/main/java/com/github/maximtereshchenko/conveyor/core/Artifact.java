package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Set;

interface Artifact {

    Id id();

    SemanticVersion semanticVersion();

    Path path();

    Set<Artifact> dependencies();
}
