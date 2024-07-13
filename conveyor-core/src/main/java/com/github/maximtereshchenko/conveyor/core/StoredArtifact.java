package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.DependencyScope;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

abstract class StoredArtifact implements Artifact {

    private final ArtifactModel artifactModel;
    private final Preferences preferences;
    private final Properties properties;
    private final Repositories repositories;
    private final SchematicModelFactory schematicModelFactory;
    private final PreferencesFactory preferencesFactory;
    private final Set<Id> propagatedExclusions;

    StoredArtifact(
        ArtifactModel artifactModel,
        Preferences preferences,
        Properties properties,
        Repositories repositories,
        SchematicModelFactory schematicModelFactory,
        PreferencesFactory preferencesFactory,
        Set<Id> propagatedExclusions
    ) {
        this.artifactModel = artifactModel;
        this.preferences = preferences;
        this.properties = properties;
        this.repositories = repositories;
        this.schematicModelFactory = schematicModelFactory;
        this.preferencesFactory = preferencesFactory;
        this.propagatedExclusions = propagatedExclusions;
    }

    @Override
    public Id id() {
        return artifactModel.idModel().id(properties);
    }

    @Override
    public Version version() {
        return version(artifactModel, properties, preferences);
    }

    @Override
    public Path path() {
        return repositories.jar(id(), version());
    }

    @Override
    public Set<Artifact> dependencies() {
        var inheritanceHierarchyModel = schematicModelFactory.inheritanceHierarchyModel(
            id(),
            version(),
            repositories
        );
        var schematicProperties = new Properties(inheritanceHierarchyModel.properties());
        var exclusions = new HashSet<>(propagatedExclusions);
        exclusions.addAll(artifactModel.exclusions());
        var schematicPreferences = preferencesFactory.preferences(
            inheritanceHierarchyModel.preferences(),
            schematicProperties,
            repositories
        );
        return inheritanceHierarchyModel.dependencies()
            .stream()
            .filter(dependencyModel -> !exclusions.contains(dependencyModel.idModel().id(properties)))
            .map(dependencyModel ->
                new Dependency(
                    new TransitivelyReferencedArtifact(
                        dependencyModel,
                        preferences,
                        schematicPreferences,
                        schematicProperties,
                        repositories,
                        schematicModelFactory,
                        preferencesFactory,
                        exclusions
                    ),
                    dependencyModel
                )
            )
            .filter(dependency -> dependency.scope() != DependencyScope.TEST)
            .collect(Collectors.toSet());
    }

    abstract Version version(
        ArtifactModel artifactModel,
        Properties properties,
        Preferences preferences
    );
}
