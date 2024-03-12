package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Set;

interface Artifact {

    String name();

    int version();

    Path path();

    Set<Artifact> dependencies();
}
