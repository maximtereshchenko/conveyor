package com.github.maximtereshchenko.conveyor.domain.test;

import java.nio.file.Path;

interface ArtifactBuilder {

    String name();

    int version();

    Path install(Path path);
}
