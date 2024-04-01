package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Set;

interface Artifact {

    String group();

    String name();

    SemanticVersion semanticVersion();

    Path path();

    Set<Artifact> dependencies();
}
