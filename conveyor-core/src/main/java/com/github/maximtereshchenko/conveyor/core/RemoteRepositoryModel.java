package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;
import java.util.Optional;

record RemoteRepositoryModel(
    String name,
    URI uri,
    Optional<Boolean> enabled
) implements RepositoryModel {

    @Override
    public RepositoryModel override(RepositoryModel base) {
        return switch (base) {
            case LocalDirectoryRepositoryModel ignored -> throw new IllegalArgumentException();
            case RemoteRepositoryModel model ->
                new RemoteRepositoryModel(name, uri, enabled.or(model::enabled));
        };
    }
}