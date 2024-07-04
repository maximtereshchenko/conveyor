package com.github.maximtereshchenko.conveyor.core;

import java.util.Optional;

interface Repository<T> {

    boolean hasName(String name);

    Optional<T> artifact(Id id, Version version, Classifier classifier);

    void publish(
        Id id,
        Version version,
        Classifier classifier,
        Resource resource
    );

    enum Classifier {
        SCHEMATIC_DEFINITION, JAR, POM
    }
}
