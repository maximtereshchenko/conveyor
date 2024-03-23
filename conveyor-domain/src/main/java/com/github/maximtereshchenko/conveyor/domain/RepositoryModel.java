package com.github.maximtereshchenko.conveyor.domain;

import java.util.Optional;

sealed interface RepositoryModel permits LocalDirectoryRepositoryModel, RemoteRepositoryModel {

    String name();

    Optional<Boolean> enabled();

    RepositoryModel override(RepositoryModel base);
}
