package com.github.maximtereshchenko.conveyor.domain;

import java.util.Optional;

interface ArtifactModel {

    String group();

    String name();

    Optional<String> version();
}
