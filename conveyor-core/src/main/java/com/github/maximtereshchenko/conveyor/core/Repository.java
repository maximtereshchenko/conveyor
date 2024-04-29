package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

interface Repository<T> {

    boolean hasName(String name);

    Optional<T> artifact(Id id, SemanticVersion semanticVersion, Classifier classifier);

    void publish(
        Id id,
        SemanticVersion semanticVersion,
        Classifier classifier,
        Resource resource
    );

    enum Classifier {
        SCHEMATIC_DEFINITION, JAR, POM
    }

    @FunctionalInterface
    interface Resource {

        InputStream inputStream() throws IOException;
    }
}
