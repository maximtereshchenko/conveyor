package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Subproject implements Project {

    private final Project parent;
    private final ProjectDefinition projectDefinition;

    Subproject(Project parent, ProjectDefinition projectDefinition) {
        this.parent = parent;
        this.projectDefinition = projectDefinition;
    }

    @Override
    public String name() {
        return projectDefinition.name();
    }

    @Override
    public int version() {
        return projectDefinition.version();
    }

    @Override
    public Map<String, String> properties() {
        var copy = new HashMap<>(parent.properties());
        copy.putAll(projectDefinition.properties());
        return Map.copyOf(copy);
    }

    @Override
    public Collection<PluginDefinition> plugins() {
        return merge(
            parent.plugins(),
            projectDefinition.plugins(),
            plugin -> true,
            PluginDefinition::name,
            this::merge
        );
    }

    @Override
    public Collection<DependencyDefinition> dependencies(Set<DependencyScope> scopes) {
        return merge(
            parent.dependencies(scopes),
            projectDefinition.dependencies(),
            dependency -> scopes.contains(dependency.scope()),
            DependencyDefinition::name,
            new PickSecond<>()
        );
    }

    private <T> Collection<T> merge(
        Collection<T> first,
        Collection<T> second,
        Predicate<T> filter,
        Function<T, String> classifier,
        BinaryOperator<T> combiner
    ) {
        return Stream.concat(first.stream(), second.stream())
            .filter(filter)
            .collect(Collectors.groupingBy(classifier, Collectors.reducing(combiner)))
            .values()
            .stream()
            .flatMap(Optional::stream)
            .toList();
    }

    private PluginDefinition merge(PluginDefinition declared, PluginDefinition pluginDefinition) {
        var copy = new HashMap<>(declared.configuration());
        copy.putAll(pluginDefinition.configuration());
        return new PluginDefinition(declared.name(), pluginDefinition.version(), copy);
    }
}
