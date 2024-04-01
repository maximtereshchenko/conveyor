package com.github.maximtereshchenko.conveyor.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Dependencies {

    private final Collection<Artifact> artifacts;

    private Dependencies(Collection<Artifact> artifacts) {
        this.artifacts = List.copyOf(artifacts);
    }

    Dependencies(ProjectDefinition projectDefinition) {
        this(List.of(new Root(projectDefinition)));
    }

    static Dependencies forPlugins(DirectoryRepository repository, ProjectDefinition projectDefinition) {
        return projectDefinition.plugins()
            .stream()
            .map(repository::artifactDefinition)
            .reduce(
                new Dependencies(projectDefinition),
                (dependencies, definition) -> dependencies(dependencies, repository, projectDefinition, definition),
                (first, second) -> first
            );
    }

    private static Dependencies dependencies(
        Dependencies original,
        DirectoryRepository repository,
        ArtifactDefinition affectBy,
        StoredArtifactDefinition parent
    ) {
        return parent.dependencies()
            .stream()
            .map(repository::artifactDefinition)
            .reduce(
                original.with(affectBy, parent),
                (dependencies, definition) -> dependencies(dependencies, repository, parent, definition),
                (first, second) -> first
            );
    }

    Set<ArtifactDefinition> modulePath(ProjectDefinition projectDefinition) {
        var modulePath = new HashSet<>(modulePath(projectDefinition.name(), projectDefinition.version()));
        modulePath.remove(projectDefinition);
        return Set.copyOf(modulePath);
    }

    private Dependencies with(ArtifactDefinition affectedBy, StoredArtifactDefinition storedArtifactDefinition) {
        var copy = new ArrayList<>(artifacts);
        copy.add(new Dependency(affectedBy, storedArtifactDefinition));
        return new Dependencies(copy);
    }

    private int effectiveVersion(String name) {
        return artifacts.stream()
            .filter(newRelation -> newRelation.hasName(name))
            .sorted(Comparator.comparingInt(Artifact::version).reversed())
            .filter(newRelation -> newRelation.canBeUsed(this))
            .map(Artifact::version)
            .findAny()
            .orElseThrow();
    }

    private Set<ArtifactDefinition> modulePath(String name, int version) {
        return artifacts.stream()
            .filter(newRelation -> newRelation.hasName(name))
            .filter(newRelation -> newRelation.version() == version)
            .findAny()
            .orElseThrow()
            .modulePath(this);
    }

    private static final class Dependency extends Artifact {

        private final ArtifactDefinition affectedBy;
        private final StoredArtifactDefinition storedArtifactDefinition;

        Dependency(ArtifactDefinition affectedBy, StoredArtifactDefinition storedArtifactDefinition) {
            super(storedArtifactDefinition);
            this.affectedBy = affectedBy;
            this.storedArtifactDefinition = storedArtifactDefinition;
        }

        @Override
        public boolean canBeUsed(Dependencies dependencies) {
            return dependencies.effectiveVersion(affectedBy.name()) == affectedBy.version();
        }

        @Override
        Set<String> dependsOn(Dependencies dependencies) {
            return storedArtifactDefinition.dependencies()
                .stream()
                .map(StoredDependencyDefinition::name)
                .collect(Collectors.toSet());
        }

    }

    private static final class Root extends Artifact {

        private final ProjectDefinition projectDefinition;

        Root(ProjectDefinition projectDefinition) {
            super(projectDefinition);
            this.projectDefinition = projectDefinition;
        }

        @Override
        public boolean canBeUsed(Dependencies dependencies) {
            return true;
        }

        @Override
        Set<String> dependsOn(Dependencies dependencies) {
            return projectDefinition.plugins()
                .stream()
                .map(PluginDefinition::name)
                .collect(Collectors.toSet());
        }
    }

    private abstract static class Artifact {

        private final ArtifactDefinition artifactDefinition;

        Artifact(ArtifactDefinition artifactDefinition) {
            this.artifactDefinition = artifactDefinition;
        }

        boolean hasName(String name) {
            return artifactDefinition.name().equals(name);
        }

        int version() {
            return artifactDefinition.version();
        }

        abstract boolean canBeUsed(Dependencies dependencies);

        abstract Set<String> dependsOn(Dependencies dependencies);

        Set<ArtifactDefinition> modulePath(Dependencies dependencies) {
            return Stream.concat(
                    Stream.of(artifactDefinition),
                    dependsOn(dependencies)
                        .stream()
                        .map(name -> dependencies.modulePath(name, dependencies.effectiveVersion(name)))
                        .flatMap(Collection::stream)
                )
                .collect(Collectors.toSet());
        }
    }
}
