package com.github.maximtereshchenko.conveyor.domain;


import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

final class ModulePath {

    private final Map<String, SortedSet<ArtifactRelation>> relations;

    private ModulePath(Map<String, SortedSet<ArtifactRelation>> relations) {
        this.relations = relations;
    }

    private ModulePath() {
        this(Map.of());
    }

    static ModulePath from(Set<? extends Artifact> artifacts) {
        return artifacts.stream()
            .map(Root::new)
            .reduce(new ModulePath(), ModulePath::with, (a, b) -> a);
    }

    Set<Path> resolved() {
        return relations.keySet()
            .stream()
            .map(this::resolved)
            .flatMap(Optional::stream)
            .map(Artifact::path)
            .collect(Collectors.toSet());
    }

    private Optional<Artifact> resolved(String name) {
        return relations.get(name)
            .stream()
            .map(relation -> relation.resolved(this))
            .flatMap(Optional::stream)
            .findFirst();
    }

    private ModulePath with(ArtifactRelation artifactRelation) {
        var copy = new HashMap<String, SortedSet<ArtifactRelation>>();
        for (var entry : relations.entrySet()) {
            copy.put(entry.getKey(), new TreeSet<>(entry.getValue()));
        }
        copy.computeIfAbsent(artifactRelation.name(), key -> new TreeSet<>(this::comparedByVersionDescending))
            .add(artifactRelation);
        return artifactRelation.edges()
            .stream()
            .reduce(new ModulePath(copy), ModulePath::with, (a, b) -> a);
    }

    private int comparedByVersionDescending(ArtifactRelation first, ArtifactRelation second) {
        return Integer.compare(second.artifact().version(), first.artifact().version());
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

    private abstract static class ArtifactRelation {

        private final Artifact artifact;

        ArtifactRelation(Artifact artifact) {
            this.artifact = artifact;
        }

        abstract Optional<Artifact> resolved(ModulePath modulePath);

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
