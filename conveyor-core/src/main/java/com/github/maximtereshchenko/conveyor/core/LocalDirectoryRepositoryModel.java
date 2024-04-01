package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Optional;

record LocalDirectoryRepositoryModel(
    String name,
    Path path,
    Optional<Boolean> enabled
) implements RepositoryModel {

    @Override
    public RepositoryModel override(RepositoryModel base) {
        return switch (base) {
            case LocalDirectoryRepositoryModel model ->
                new LocalDirectoryRepositoryModel(name, path, enabled.or(model::enabled));
            case RemoteRepositoryModel ignored -> throw new IllegalArgumentException();
        };
    }
}
