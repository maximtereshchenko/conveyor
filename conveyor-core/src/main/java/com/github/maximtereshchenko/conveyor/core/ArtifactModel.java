package com.github.maximtereshchenko.conveyor.core;

import java.util.Optional;

interface ArtifactModel {

    Id id();

    Optional<String> version();
}
