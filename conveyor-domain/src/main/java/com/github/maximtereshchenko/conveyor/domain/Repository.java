package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Optional;

interface Repository {

    Optional<Path> path(
        String group,
        String name,
        SemanticVersion semanticVersion,
        Classifier classifier
    );

    enum Classifier {SCHEMATIC_DEFINITION, MODULE}
}
