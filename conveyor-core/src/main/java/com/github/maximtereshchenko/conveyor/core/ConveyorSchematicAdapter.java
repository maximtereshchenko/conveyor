package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class ConveyorSchematicAdapter implements ConveyorSchematic {

    private final Path path;
    private final Id id;
    private final Version version;
    private final Properties properties;
    private final Dependencies dependencies;
    private final Repositories repositories;

    ConveyorSchematicAdapter(
        Path path, Id id,
        Version version,
        Properties properties,
        Dependencies dependencies,
        Repositories repositories
    ) {
        this.path = path;
        this.id = id;
        this.version = version;
        this.properties = properties;
        this.dependencies = dependencies;
        this.repositories = repositories;
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Optional<String> propertyValue(String key) {
        return properties.value(key);
    }

    @Override
    public Set<Path> classpath(Set<DependencyScope> scopes) {
        return dependencies.classpath(scopes);
    }

    @Override
    public void publish(String repository, Path path, ArtifactClassifier artifactClassifier) {
        repositories.publish(
            repository,
            id,
            version,
            switch (artifactClassifier) {
                case SCHEMATIC_DEFINITION -> Repository.Classifier.SCHEMATIC_DEFINITION;
                case JAR -> Repository.Classifier.JAR;
            },
            path
        );
    }
}
