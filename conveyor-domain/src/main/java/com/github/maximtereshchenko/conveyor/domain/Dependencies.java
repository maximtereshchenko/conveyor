package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Dependencies {

    private final ArtifactDefinition root;
    private final Collection<Artifact> artifacts;

    private Dependencies(ArtifactDefinition root, Collection<Artifact> artifacts) {
        this.root = root;
        this.artifacts = List.copyOf(artifacts);
    }

    static Dependencies from(
        DirectoryRepository repository,
        Project project,
        Collection<? extends ArtifactDefinition> artifactDefinitions
    ) {
        return dependencies(
            repository,
            new Dependencies(
                project,
                List.of(new Root(project, artifactDefinitions))
            ),
            project,
            artifactDefinitions
        );
    }

    private static Dependencies dependencies(
        DirectoryRepository repository,
        Dependencies original,
        ArtifactDefinition parent,
        Collection<? extends ArtifactDefinition> definitions
    ) {
        return definitions.stream()
            .map(repository::projectDefinition)
            .reduce(
                original,
                (dependencies, definition) ->
                    dependencies(
                        repository,
                        dependencies,
                        definition,
                        definition.dependencies()
                            .stream()
                            .filter(dependencyDefinition -> dependencyDefinition.scope() != DependencyScope.TEST)
                            .toList()
                    )
                        .with(parent, definition),
                new PickSecond<>()
            );
    }

    Set<ArtifactDefinition> modulePath() {
        var modulePath = new HashSet<>(modulePath(root.name(), root.version()));
        modulePath.remove(root);
        return Set.copyOf(modulePath);
    }

    private Dependencies with(ArtifactDefinition affectedBy, ProjectDefinition projectDefinition) {
        var copy = new ArrayList<>(artifacts);
        copy.add(new Dependency(affectedBy, projectDefinition));
        return new Dependencies(root, copy);
    }

    private int effectiveVersion(String name) {
        return artifacts.stream()
            .filter(artifact -> artifact.hasName(name))
            .sorted(Comparator.comparingInt(Artifact::version).reversed())
            .filter(artifact -> artifact.canBeUsed(this))
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
        private final ProjectDefinition projectDefinition;

        Dependency(ArtifactDefinition affectedBy, ProjectDefinition projectDefinition) {
            super(projectDefinition);
            this.affectedBy = affectedBy;
            this.projectDefinition = projectDefinition;
        }

        @Override
        public boolean canBeUsed(Dependencies dependencies) {
            return dependencies.effectiveVersion(affectedBy.name()) == affectedBy.version();
        }

        @Override
        Set<String> dependsOn() {
            return projectDefinition.dependencies()
                .stream()
                .filter(dependencyDefinition -> dependencyDefinition.scope() != DependencyScope.TEST)
                .map(DependencyDefinition::name)
                .collect(Collectors.toSet());
        }

    }

    private static final class Root extends Artifact {

        private final Collection<ArtifactDefinition> dependencies;

        Root(Project project, Collection<? extends ArtifactDefinition> dependencies) {
            super(project);
            this.dependencies = List.copyOf(dependencies);
        }

        @Override
        boolean canBeUsed(Dependencies dependencies) {
            return true;
        }

        @Override
        Set<String> dependsOn() {
            return dependencies.stream()
                .map(ArtifactDefinition::name)
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

        abstract Set<String> dependsOn();

        Set<ArtifactDefinition> modulePath(Dependencies dependencies) {
            return Stream.concat(
                    Stream.of(artifactDefinition),
                    dependsOn()
                        .stream()
                        .map(name -> dependencies.modulePath(name, dependencies.effectiveVersion(name)))
                        .flatMap(Collection::stream)
                )
                .collect(Collectors.toSet());
        }
    }
}
