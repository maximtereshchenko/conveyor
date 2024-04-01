package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;

final class PackagedArtifact implements Artifact {

    private final String name;
    private final int version;
    private final Repository repository;

    PackagedArtifact(String name, int version, Repository repository) {
        this.name = name;
        this.version = version;
        this.repository = repository;
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
        return repository.manualDefinition(name, version)
            .dependencies()
            .stream()
            .filter(definition -> definition.scope() != DependencyScope.TEST)
            .map(definition -> new PackagedArtifact(definition.name(), definition.version(), repository))
            .collect(new ImmutableSetCollector<>());
    }

    @Override
    public Path modulePath() {
        return repository.path(name, version);
    }
}
