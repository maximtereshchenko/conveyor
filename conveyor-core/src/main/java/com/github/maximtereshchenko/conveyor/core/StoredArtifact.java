package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

abstract class StoredArtifact implements Artifact {

    private final ArtifactModel artifactModel;
    private final Preferences preferences;
    private final Properties properties;
    private final Repositories repositories;
    private final SchematicModelFactory schematicModelFactory;
    private final PreferencesFactory preferencesFactory;

    StoredArtifact(
        ArtifactModel artifactModel,
        Preferences preferences,
        Properties properties,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory,
        PreferencesFactory preferencesFactory
    ) {
        this.artifactModel = artifactModel;
        this.preferences = preferences;
        this.properties = properties;
        this.repositories = repositories;
        this.schematicModelFactory = schematicModelFactory;
        this.preferencesFactory = preferencesFactory;
    }

    @Override
    public Id id() {
        return artifactModel.id();
    }

    @Override
    public SemanticVersion semanticVersion() {
        return semanticVersion(artifactModel, properties, preferences);
    }

    @Override
    public Path path() {
        return repositories.module(artifactModel.id(), semanticVersion());
    }

    @Override
    public Set<Artifact> dependencies() {
        var inheritanceHierarchyModel = schematicModelFactory.inheritanceHierarchyModel(
            artifactModel.id(),
            semanticVersion(),
            repositories
        );
        var schematicProperties = new Properties(inheritanceHierarchyModel.properties());
        return inheritanceHierarchyModel.dependencies()
            .stream()
            .map(dependencyModel ->
                new Dependency(
                    new TransitivelyReferencedArtifact(
                        dependencyModel,
                        preferences,
                        preferencesFactory.preferences(
                            inheritanceHierarchyModel.preferences(),
                            schematicProperties,
                            repositories
                        ),
                        schematicProperties,
                        repositories,
                        schematicModelFactory,
                        preferencesFactory
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
