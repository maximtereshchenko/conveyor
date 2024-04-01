package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

final class ModulePath {

    private final ImmutableMap<String, ImmutableSortedSet<ArtifactRelation>> relations;

    private ModulePath(ImmutableMap<String, ImmutableSortedSet<ArtifactRelation>> relations) {
        this.relations = relations;
    }

    private ModulePath() {
        this(new ImmutableMap<>());
    }

    static ModulePath from(ImmutableCollection<? extends Artifact> artifacts) {
        return artifacts.stream()
            .map(Root::new)
            .reduce(new ModulePath(), ModulePath::with, new PickSecond<>());
    }

    ImmutableSet<Path> modulePath() {
        return relations.keys()
            .stream()
            .map(this::resolved)
            .flatMap(Optional::stream)
            .map(Artifact::modulePath)
            .collect(new ImmutableSetCollector<>());
    }

    private Optional<Artifact> resolved(String name) {
        return relations.value(name)
            .stream()
            .flatMap(ImmutableSortedSet::stream)
            .map(relation -> relation.resolved(this))
            .flatMap(Optional::stream)
            .findFirst();
    }

    private ModulePath with(ArtifactRelation artifactRelation) {
        return artifactRelation.edges()
            .stream()
            .reduce(
                new ModulePath(
                    relations.compute(
                        artifactRelation.name(),
                        ImmutableSortedSet::new,
                        set -> set.with(artifactRelation)
                    )
                ),
                ModulePath::with,
                new PickSecond<>()
            );
    }

    private static final class Edge extends ArtifactRelation {

        private final Artifact requirement;

        Edge(Artifact requirement, Artifact artifact) {
            super(artifact);
            this.requirement = requirement;
        }

        @Override
        Optional<Artifact> resolved(ModulePath modulePath) {
            if (isRequirementResolved(modulePath)) {
                return Optional.of(artifact());
            }
            return Optional.empty();
        }

        private boolean isRequirementResolved(ModulePath modulePath) {
            return modulePath.resolved(requirement.name())
                .map(requirement::equals)
                .orElse(Boolean.FALSE);
        }
    }

    private static final class Root extends ArtifactRelation {

        Root(Artifact artifact) {
            super(artifact);
        }

        @Override
        Optional<Artifact> resolved(ModulePath modulePath) {
            return Optional.of(artifact());
        }
    }

    private abstract static class ArtifactRelation implements Comparable<ArtifactRelation> {

        private final Artifact artifact;

        ArtifactRelation(Artifact artifact) {
            this.artifact = artifact;
        }

        @Override
        public int compareTo(ArtifactRelation artifactRelation) {
            return Integer.compare(artifactRelation.artifact().version(), artifact.version());
        }

        @Override
        public int hashCode() {
            return Objects.hash(artifact);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            var that = (ArtifactRelation) object;
            return Objects.equals(artifact, that.artifact);
        }

        abstract Optional<Artifact> resolved(ModulePath modulePath);

        Artifact artifact() {
            return artifact;
        }

        String name() {
            return artifact.name();
        }

        ImmutableCollection<Edge> edges() {
            return new ImmutableList<>(
                artifact.dependencies()
                    .stream()
                    .map(dependency -> new Edge(artifact, dependency))
                    .toList()
            );
        }
    }
}
