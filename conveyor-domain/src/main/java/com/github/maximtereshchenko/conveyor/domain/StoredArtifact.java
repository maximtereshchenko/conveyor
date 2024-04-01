package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

abstract class StoredArtifact implements Artifact {

    private final ArtifactModel artifactModel;
    private final Properties properties;
    private final Preferences preferences;
    private final Repositories repositories;
    private final SchematicModelFactory schematicModelFactory;

    StoredArtifact(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory
    ) {
        this.artifactModel = artifactModel;
        this.properties = properties;
        this.preferences = preferences;
        this.repositories = repositories;
        this.schematicModelFactory = schematicModelFactory;
    }

    @Override
    public String group() {
        return artifactModel.group();
    }

    @Override
    public String name() {
        return artifactModel.name();
    }

    @Override
    public SemanticVersion semanticVersion() {
        return semanticVersion(artifactModel, properties, preferences);
    }

    @Override
    public Path path() {
        return repositories.module(group(), name(), semanticVersion());
    }

    @Override
    public Set<Artifact> dependencies() {
        var hierarchicalSchematicModel = schematicModelFactory.hierarchicalSchematicModel(
            group(),
            name(),
            semanticVersion(),
            repositories
        );
        return hierarchicalSchematicModel.dependencies()
            .stream()
            .map(dependencyModel ->
                new Dependency(
                    new TransitivelyReferencedArtifact(
                        dependencyModel,
                        new Properties(hierarchicalSchematicModel.properties()),
                        preferences,
                        repositories,
                        schematicModelFactory
                    ),
                    dependencyModel
                )
            )
            .filter(dependency -> dependency.scope() != DependencyScope.TEST)
            .collect(Collectors.toSet());
    }

    abstract SemanticVersion semanticVersion(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences
    );
}
