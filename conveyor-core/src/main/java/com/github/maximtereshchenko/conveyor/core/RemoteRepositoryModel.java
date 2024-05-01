package com.github.maximtereshchenko.conveyor.core;

import java.net.URI;

record RemoteRepositoryModel(String name, URI uri) implements RepositoryModel {

    @Override
    public RepositoryModel override(RepositoryModel base) {
        return switch (base) {
            case LocalDirectoryRepositoryModel ignored -> throw new IllegalArgumentException();
            case RemoteRepositoryModel ignored -> this;
        };
    }
}