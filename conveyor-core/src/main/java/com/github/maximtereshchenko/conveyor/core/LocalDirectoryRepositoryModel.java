package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;

record LocalDirectoryRepositoryModel(String name, Path path) implements RepositoryModel {

    @Override
    public RepositoryModel override(RepositoryModel base) {
        return switch (base) {
            case LocalDirectoryRepositoryModel ignored -> this;
            case RemoteRepositoryModel ignored -> throw new IllegalArgumentException();
        };
    }
}
