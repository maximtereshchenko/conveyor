package com.github.maximtereshchenko.conveyor.core;

import java.util.Optional;

interface Repository<T> {

    Optional<T> artifact(Id id, SemanticVersion semanticVersion, Classifier classifier);

    enum Classifier {
        SCHEMATIC_DEFINITION, MODULE, POM
    }
}
