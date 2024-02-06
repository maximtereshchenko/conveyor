package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematicDependencies;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Dependencies implements ConveyorSchematicDependencies {

    private final ImmutableMap<String, Dependency> byName;

    private Dependencies(ImmutableMap<String, Dependency> byName) {
        this.byName = byName;
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

    @Override
    public Set<Path> modulePath(DependencyScope... scopes) {
        return ModulePath.from(
                byName.values()
                    .stream()
                    .filter(artifact -> artifact.hasAny(scopes))
                    .collect(new ImmutableSetCollector<>())
            )
            .modulePath()
            .stream()
            .collect(Collectors.toSet());
    }

    Stream<Dependency> stream() {
        return byName.values().stream();
    }

    Dependencies with(Dependencies dependencies) {
        return new Dependencies(byName.withAll(dependencies.byName));
    }

    boolean contains(String name) {
        return byName.containsKey(name);
    }
}
