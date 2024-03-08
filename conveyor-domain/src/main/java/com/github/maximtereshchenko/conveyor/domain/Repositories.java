package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

final class Repositories {

    private final ImmutableMap<String, Repository> indexed;

    private Repositories(ImmutableMap<String, Repository> indexed) {
        this.indexed = indexed;
    }

    Repositories() {
        this(new ImmutableMap<>());
    }

    static Repositories from(ImmutableSet<Repository> repositories) {
        return new Repositories(
            repositories.stream()
                .collect(new ImmutableMapCollector<>(Repository::name))
        );
    }

    Repositories override(Repositories repositories) {
        return new Repositories(repositories.indexed.withAll(indexed));
    }

    ManualDefinition manualDefinition(String name, int version) {
        return find(repository -> repository.manualDefinition(name, version));
    }

    Path path(String name, int version) {
        return find(repository -> repository.path(name, version));
    }

    private <T> T find(Function<Repository, Optional<T>> function) {
        return indexed.values()
            .stream()
            .filter(Repository::isEnabled)
            .map(function)
            .flatMap(Optional::stream)
            .findAny()
            .orElseThrow();
    }
}
