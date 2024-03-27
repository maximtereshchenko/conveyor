package com.github.maximtereshchenko.conveyor.domain;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

interface Repository {

    Optional<Path> path(URI uri, Classifier classifier);

    enum Classifier {SCHEMATIC_DEFINITION, MODULE}
}
