package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

final class Dependencies {

    private final Map<String, TreeSet<ArtifactRelation>> relations;

    private Dependencies(Map<String, TreeSet<ArtifactRelation>> relations) {
        this.relations = relations;
    }

    private Dependencies() {
        this(Map.of());
    }

    static Dependencies from(Collection<Artifact> artifacts) {
        return artifacts.stream()
            .map(Root::new)
            .reduce(new Dependencies(), Dependencies::with, new PickSecond<>());
    }

    Set<Path> modulePath() {
        return relations.keySet()
            .stream()
            .map(this::resolved)
            .flatMap(Optional::stream)
            .map(Artifact::modulePath)
            .collect(Collectors.toSet());
    }

    private Map<String, TreeSet<ArtifactRelation>> deepCopy() {
        var copy = new HashMap<String, TreeSet<ArtifactRelation>>();
        for (var entry : relations.entrySet()) {
            copy.put(entry.getKey(), new TreeSet<>(entry.getValue()));
        }
        return copy;
    }

    private Optional<Artifact> resolved(String name) {
        return relations.get(name)
            .stream()
            .map(relation -> relation.resolved(this))
            .flatMap(Optional::stream)
            .findFirst();
    }

    private Dependencies with(ArtifactRelation artifactRelation) {
        var deepCopy = deepCopy();
        walkArtifactTree(artifactRelation, deepCopy);
        return new Dependencies(deepCopy);
    }

    private void walkArtifactTree(ArtifactRelation relation, Map<String, TreeSet<ArtifactRelation>> collected) {
        collected.computeIfAbsent(relation.name(), key -> new TreeSet<>()).add(relation);
        for (var edge : relation.edges()) {
            walkArtifactTree(edge, collected);
        }
    }

    private static final class Edge extends ArtifactRelation {

        private final Artifact requirement;

        Edge(Artifact requirement, Artifact artifact) {
            super(artifact);
            this.requirement = requirement;
        }

        @Override
        Optional<Artifact> resolved(Dependencies dependencies) {
            if (isRequirementResolved(dependencies)) {
                return Optional.of(artifact());
            }
            return Optional.empty();
        }

        private boolean isRequirementResolved(Dependencies dependencies) {
            return dependencies.resolved(requirement.name())
                .map(requirement::equals)
                .orElse(Boolean.FALSE);
        }
    }

    private static final class Root extends ArtifactRelation {

        Root(Artifact artifact) {
            super(artifact);
        }

        @Override
        Optional<Artifact> resolved(Dependencies dependencies) {
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

        abstract Optional<Artifact> resolved(Dependencies dependencies);

        Artifact artifact() {
            return artifact;
        }

        String name() {
            return artifact.name();
        }

        Collection<Edge> edges() {
            return artifact.dependencies()
                .stream()
                .map(dependency -> new Edge(artifact, dependency))
                .toList();
        }
    }
}
