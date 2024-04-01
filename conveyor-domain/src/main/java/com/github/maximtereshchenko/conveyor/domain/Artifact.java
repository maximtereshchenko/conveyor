package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;

interface Artifact {

    String name();

    int version();

    Dependencies dependencies();

    Path modulePath();
}
