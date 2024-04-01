package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Optional;

record RepositoryModel(String name, Path path, Optional<Boolean> enabled) {

    RepositoryModel override(RepositoryModel base) {
        return new RepositoryModel(name, path, enabled.or(base::enabled));
    }
}
