package com.github.maximtereshchenko.conveyor.core;

import java.util.Optional;

interface Repository<I, O> {

    void publish(
        Id id,
        Version version,
        Classifier classifier,
        I artifact
    );

    Optional<O> artifact(Id id, Version version, Classifier classifier);

    enum Classifier {
        SCHEMATIC_DEFINITION, CLASSES, POM
    }
}
