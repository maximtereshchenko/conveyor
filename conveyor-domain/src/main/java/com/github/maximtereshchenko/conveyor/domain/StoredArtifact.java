package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;

abstract class StoredArtifact<T extends DependencyModel> extends DependentArtifact<T> {

    private final Repositories repositories;

    StoredArtifact(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    public Path path() {
        return repositories.path(name(), version());
    }

    Repositories repositories() {
        return repositories;
    }
}
