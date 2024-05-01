package com.github.maximtereshchenko.conveyor.core;

sealed interface RepositoryModel permits LocalDirectoryRepositoryModel, RemoteRepositoryModel {

    String name();

    RepositoryModel override(RepositoryModel base);
}
