package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.plugin.api.ArtifactClassifier;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

final class ConveyorSchematicAdapter implements ConveyorSchematic {

    private final Id id;
    private final SemanticVersion semanticVersion;
    private final Properties properties;
    private final Dependencies dependencies;
    private final Repositories repositories;

    ConveyorSchematicAdapter(
        Id id,
        SemanticVersion semanticVersion,
        Properties properties,
        Dependencies dependencies,
        Repositories repositories
    ) {
        this.id = id;
        this.semanticVersion = semanticVersion;
        this.properties = properties;
        this.dependencies = dependencies;
        this.repositories = repositories;
    }

    @Override
    public SchematicCoordinates coordinates() {
        return id.coordinates(semanticVersion);
    }

    @Override
    public Path discoveryDirectory() {
        return properties.discoveryDirectory();
    }

    @Override
    public Path constructionDirectory() {
        return properties.constructionDirectory();
    }

    @Override
    public Optional<String> propertyValue(String key) {
        return properties.value(key);
    }

    @Override
    public Set<Path> classPath(Set<DependencyScope> scopes) {
        return dependencies.classPath(scopes);
    }

    @Override
    public void publish(String repository, Path path, ArtifactClassifier artifactClassifier) {
        repositories.publish(
            repository,
            id,
            semanticVersion,
            switch (artifactClassifier) {
                case SCHEMATIC_DEFINITION -> Repository.Classifier.SCHEMATIC_DEFINITION;
                case JAR -> Repository.Classifier.JAR;
            },
            path
        );
    }
}
