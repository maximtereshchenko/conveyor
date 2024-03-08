package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

final class Dependencies {

    private final ImmutableMap<String, Dependency> indexed;

    private Dependencies(ImmutableMap<String, Dependency> indexed) {
        this.indexed = indexed;
    }

    Dependencies() {
        this(new ImmutableMap<>());
    }

    static Dependencies from(ImmutableSet<Dependency> dependencies) {
        return new Dependencies(
            dependencies.stream()
                .collect(new ImmutableMapCollector<>(Dependency::name))
        );
    }

    Set<Path> modulePath(Repositories repositories, ImmutableSet<DependencyScope> scopes) {
        return ModulePath.from(
                indexed.values()
                    .stream()
                    .filter(dependency -> dependency.in(scopes))
                    .map(dependency -> dependency.artifact(repositories))
                    .collect(new ImmutableSetCollector<>())
            )
            .resolved()
            .stream()
            .collect(Collectors.toSet());
    }

    Dependencies override(Dependencies base) {
        return new Dependencies(base.indexed.withAll(indexed));
    }
}
