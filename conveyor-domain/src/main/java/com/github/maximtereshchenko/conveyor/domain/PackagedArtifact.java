package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;

final class PackagedArtifact implements Artifact {

    private final String name;
    private final int version;
    private final Repositories repositories;

    PackagedArtifact(String name, int version, Repositories repositories) {
        this.name = name;
        this.version = version;
        this.repositories = repositories;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public ImmutableSet<Artifact> dependencies() {
        return repositories.manualDefinition(name, version)
            .dependencies()
            .stream()
            .filter(definition -> definition.scope() != DependencyScope.TEST)
            .map(definition -> new PackagedArtifact(definition.name(), definition.version(), repositories))
            .collect(new ImmutableSetCollector<>());
    }

    @Override
    public Path modulePath() {
        return repositories.path(name, version);
    }
}
