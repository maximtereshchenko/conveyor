package com.github.maximtereshchenko.conveyor.core;

import java.util.Optional;
import java.util.Set;

interface ArtifactModel {

    IdModel idModel();

    Optional<String> version();

    Set<Id> exclusions();
}
