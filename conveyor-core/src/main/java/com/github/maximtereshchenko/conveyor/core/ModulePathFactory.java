package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ModulePathFactory {

    Set<Path> modulePath(Set<? extends Artifact> artifacts) {
        var relations = relations(artifacts);
        return relations.ids()
            .map(relations::resolved)
            .flatMap(Optional::stream)
            .map(Artifact::path)
            .collect(Collectors.toSet());
    }

    private Relations relations(Set<? extends Artifact> artifacts) {
        var relations = new Relations();
        for (var artifact : artifacts) {
            relations.add(new Root(artifact));
        }
        return relations;
    }

    private static final class Edge extends ArtifactRelation {

        private final Artifact requirement;

        Edge(Artifact requirement, Artifact artifact) {
            super(artifact);
            this.requirement = requirement;
        }

        @Override
        Optional<Artifact> resolved(Relations relations) {
            if (isRequirementResolved(relations)) {
                return Optional.of(artifact());
            }
            return Optional.empty();
        }

        private boolean isRequirementResolved(Relations relations) {
            return relations.resolved(requirement.id())
                .map(requirement::equals)
                .orElse(Boolean.FALSE);
        }
    }

    private static final class Root extends ArtifactRelation {

        Root(Artifact artifact) {
            super(artifact);
        }

        @Override
        Optional<Artifact> resolved(Relations relations) {
            return Optional.of(artifact());
        }
    }

    private abstract static class ArtifactRelation {

        private final Artifact artifact;

        ArtifactRelation(Artifact artifact) {
            this.artifact = artifact;
        }

        abstract Optional<Artifact> resolved(Relations relations);

        Artifact artifact() {
            return artifact;
        }

        Collection<Edge> edges() {
            return artifact.dependencies()
                .stream()
                .map(dependency -> new Edge(artifact, dependency))
                .toList();
        }
    }

    private static class Relations {

        private final Map<Id, SortedSet<ArtifactRelation>> indexed = new HashMap<>();

        void add(ArtifactRelation artifactRelation) {
            var artifact = artifactRelation.artifact();
            indexed.computeIfAbsent(artifact.id(), key -> new TreeSet<>(comparator()))
                .add(artifactRelation);
            for (var edge : artifactRelation.edges()) {
                add(edge);
            }
        }

        Stream<Id> ids() {
            return indexed.keySet().stream();
        }

        Optional<Artifact> resolved(Id id) {
            return indexed.get(id)
                .stream()
                .map(relation -> relation.resolved(this))
                .flatMap(Optional::stream)
                .findFirst();
        }

        private Comparator<ArtifactRelation> comparator() {
            return Comparator.<ArtifactRelation, SemanticVersion>comparing(
                    relation -> relation.artifact().semanticVersion()
                )
                .reversed();
        }
    }
}