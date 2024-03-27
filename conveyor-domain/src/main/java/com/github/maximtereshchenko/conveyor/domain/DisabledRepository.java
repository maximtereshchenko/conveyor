package com.github.maximtereshchenko.conveyor.domain;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

final class DisabledRepository implements Repository {

    @Override
    public Optional<Path> path(URI uri, Classifier classifier) {
        return Optional.empty();
    }
}
