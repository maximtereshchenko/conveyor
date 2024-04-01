package com.github.maximtereshchenko.conveyor.core;

import java.net.URL;
import java.util.Optional;

record RemoteRepositoryModel(
    String name,
    URL url,
    Optional<Boolean> enabled
) implements RepositoryModel {

    @Override
    public RepositoryModel override(RepositoryModel base) {
        return switch (base) {
            case LocalDirectoryRepositoryModel ignored -> throw new IllegalArgumentException();
            case RemoteRepositoryModel model ->
                new RemoteRepositoryModel(name, url, enabled.or(model::enabled));
        };
    }
}