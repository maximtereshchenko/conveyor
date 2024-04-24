package com.github.maximtereshchenko.conveyor.core;

import java.util.Optional;

interface ArtifactModel {

    IdModel idModel();

    Optional<String> version();
}
