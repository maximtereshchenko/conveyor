package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

final class ModulePathBuilder {

    Collection<Path> pluginsModulePath(DirectoryRepository repository, Collection<PluginDefinition> plugins) {
        return modulePath(
            repository,
            plugins.stream()
                .map(repository::artifactDefinition)
                .toList()
        );
    }

    private void walkArtifactTree(
        List<ArtifactDefinition> initial,
        Consumer<ArtifactDefinition> elementConsumer,
        Function<DependencyDefinition, Optional<ArtifactDefinition>> nextElementFunction
    ) {
        var queue = new LinkedList<>(initial);
        while (!queue.isEmpty()) {
            var artifactDefinition = queue.poll();
            elementConsumer.accept(artifactDefinition);
            artifactDefinition.dependencies()
                .stream()
                .map(nextElementFunction)
                .flatMap(Optional::stream)
                .forEach(queue::offer);
        }
    }

    private Collection<Path> modulePath(DirectoryRepository repository, List<ArtifactDefinition> initial) {
        var resolvedVersions = resolvedVersions(repository, initial);
        var artifacts = new HashSet<Path>();
        walkArtifactTree(
            initial,
            artifactDefinition -> artifacts.add(repository.artifact(artifactDefinition)),
            dependencyDefinition ->
                Optional.of(
                    repository.artifactDefinition(
                        dependencyDefinition.name(),
                        resolvedVersions.get(dependencyDefinition.name())
                    )
                )
        );
        return Set.copyOf(artifacts);
    }

    private Map<String, Integer> resolvedVersions(DirectoryRepository repository, List<ArtifactDefinition> initial) {
        var resolvedVersions = new HashMap<String, Integer>();
        walkArtifactTree(
            initial,
            artifactDefinition -> resolvedVersions.put(artifactDefinition.name(), artifactDefinition.version()),
            dependencyDefinition -> withUnresolvedVersion(dependencyDefinition, resolvedVersions, repository)
        );
        return Map.copyOf(resolvedVersions);
    }

    private Optional<ArtifactDefinition> withUnresolvedVersion(
        DependencyDefinition dependencyDefinition,
        Map<String, Integer> resolvedVersions,
        DirectoryRepository repository
    ) {
        if (hasUnresolvedVersion(resolvedVersions, dependencyDefinition)) {
            return Optional.of(repository.artifactDefinition(dependencyDefinition));
        }
        return Optional.empty();
    }

    private boolean hasUnresolvedVersion(
        Map<String, Integer> resolvedVersions,
        DependencyDefinition definition
    ) {
        return resolvedVersions.getOrDefault(definition.name(), 0) < definition.version();
    }
}
