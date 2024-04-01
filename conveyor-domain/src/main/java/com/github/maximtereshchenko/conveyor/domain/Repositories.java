package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

final class Repositories {

    private final Set<Repository> all;

    Repositories(Set<Repository> all) {
        this.all = all;
    }

    ManualDefinition manualDefinition(String name, SemanticVersion version) {
        return find(repository -> repository.manualDefinition(name, version));
    }

    Path path(String name, SemanticVersion version) {
        return find(repository -> repository.path(name, version));
    }

    private <T> T find(Function<Repository, Optional<T>> function) {
        return all.stream()
            .map(function)
            .flatMap(Optional::stream)
            .findAny()
            .orElseThrow();
    }
}
