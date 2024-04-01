package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

interface Repository {

    Optional<Path> path(Id id, SemanticVersion semanticVersion, Classifier classifier);

    enum Classifier {SCHEMATIC_DEFINITION, MODULE}
}
