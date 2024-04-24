package com.github.maximtereshchenko.conveyor.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class PreferencesFactory {

    private final SchematicModelFactory schematicModelFactory;

    PreferencesFactory(SchematicModelFactory schematicModelFactory) {
        this.schematicModelFactory = schematicModelFactory;
    }

    Preferences preferences(
        PreferencesModel preferencesModel,
        Properties properties,
        Repositories repositories
    ) {
        return new Preferences(versions(preferencesModel, properties, repositories));
    }

    private Map<Id, SemanticVersion> versions(
        PreferencesModel preferencesModel,
        Properties properties,
        Repositories repositories
    ) {
        return Stream.of(
                includedPreferences(preferencesModel.inclusions(), properties, repositories),
                artifactPreferences(preferencesModel.artifacts(), properties)
            )
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (previous, next) -> next)
            );
    }

    private Map<Id, SemanticVersion> includedPreferences(
        Set<PreferencesInclusionModel> inclusions,
        Properties properties,
        Repositories repositories
    ) {
        return inclusions.stream()
            .map(preferencesInclusionModel ->
                schematicModelFactory.inheritanceHierarchyModel(
                    preferencesInclusionModel.idModel().id(properties),
                    new SemanticVersion(
                        properties.interpolated(preferencesInclusionModel.version())
                    ),
                    repositories
                )
            )
            .map(inheritanceHierarchyModel ->
                versions(
                    inheritanceHierarchyModel.preferences(),
                    new Properties(inheritanceHierarchyModel.properties()),
                    repositories
                )
            )
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    this::highestVersion
                )
            );
    }

    private SemanticVersion highestVersion(
        SemanticVersion first,
        SemanticVersion second
    ) {
        if (first.compareTo(second) > 0) {
            return first;
        }
        return second;
    }

    private Map<Id, SemanticVersion> artifactPreferences(
        Set<ArtifactPreferenceModel> artifacts,
        Properties properties
    ) {
        return artifacts.stream()
            .collect(
                Collectors.toMap(
                    artifactPreferenceModel -> artifactPreferenceModel.idModel().id(properties),
                    artifactPreferenceModel -> new SemanticVersion(
                        properties.interpolated(artifactPreferenceModel.version())
                    )
                )
            );
    }
}
