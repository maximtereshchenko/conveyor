package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Products;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Schematic {

    private final PartialSchematicHierarchy partialSchematicHierarchy;
    private final DefinitionReader definitionReader;
    private final ModelFactory modelFactory;
    private final ModulePathFactory modulePathFactory;

    Schematic(
        PartialSchematicHierarchy partialSchematicHierarchy,
        DefinitionReader definitionReader,
        ModelFactory modelFactory, ModulePathFactory modulePathFactory
    ) {
        this.partialSchematicHierarchy = partialSchematicHierarchy;
        this.definitionReader = definitionReader;
        this.modelFactory = modelFactory;
        this.modulePathFactory = modulePathFactory;
    }

    String name() {
        return partialSchematicHierarchy.name();
    }

    boolean locatedAt(Path path) {
        return partialSchematicHierarchy.path().equals(path);
    }

    boolean inheritsFrom(Schematic schematic) {
        return partialSchematicHierarchy.inheritsFrom(schematic.partialSchematicHierarchy);
    }

    boolean dependsOn(Schematic schematic, Schematics schematics) {
        return partialSchematicHierarchy.dependencies()
            .stream()
            .map(this::schematicDependencyModel)
            .flatMap(Optional::stream)
            .anyMatch(schematicDependencyModel ->
                schematicDependencyModel.name().equals(schematic.name()) ||
                schematics.haveDependencyBetween(schematicDependencyModel.name(), schematic)
            );
    }

    SchematicProducts construct(SchematicProducts schematicProducts, Stage stage) {
        var repositories = repositories();
        var fullSchematicHierarchy = modelFactory.fullSchematicHierarchy(partialSchematicHierarchy, repositories);
        var properties = properties(fullSchematicHierarchy);
        var preferences = preferences(fullSchematicHierarchy, repositories, properties);
        return schematicProducts
            .with(
                fullSchematicHierarchy.name(),
                plugins(
                    fullSchematicHierarchy,
                    properties,
                    preferences,
                    repositories,
                    dependencies(fullSchematicHierarchy, properties, preferences, repositories, schematicProducts)
                )
                    .executeTasks(
                        new Products()
                            .with(fullSchematicHierarchy.path(), ProductType.SCHEMATIC_DEFINITION),
                        stage
                    )
            );
    }

    private Dependencies dependencies(
        FullSchematicHierarchy fullSchematicHierarchy,
        Properties properties,
        Preferences preferences,
        Repositories repositories,
        SchematicProducts schematicProducts
    ) {
        return new Dependencies(
            fullSchematicHierarchy.dependencies()
                .stream()
                .map(dependencyModel ->
                    dependency(dependencyModel, properties, preferences, repositories, schematicProducts)
                )
                .collect(Collectors.toSet()),
            modulePathFactory
        );
    }

    private Dependency dependency(
        DependencyModel dependencyModel,
        Properties properties,
        Preferences preferences,
        Repositories repositories,
        SchematicProducts schematicProducts
    ) {
        return switch (dependencyModel) {
            case ArtifactDependencyModel model ->
                new DirectDependency(model, modelFactory, properties, preferences, repositories);
            case SchematicDependencyModel model ->
                new SchematicDependency(model, schematicProducts, modelFactory, repositories, preferences, properties);
        };
    }

    private Optional<SchematicDependencyModel> schematicDependencyModel(DependencyModel dependencyModel) {
        if (dependencyModel instanceof SchematicDependencyModel model) {
            return Optional.of(model);
        }
        return Optional.empty();
    }

    private Repositories repositories() {
        return new Repositories(
            partialSchematicHierarchy.repositories()
                .stream()
                .map(repositoryModel -> repository(repositoryModel, partialSchematicHierarchy.path()))
                .collect(Collectors.toSet())
        );
    }

    private Repository repository(RepositoryModel repositoryModel, Path path) {
        if (repositoryModel.enabled().orElse(Boolean.TRUE)) {
            return new EnabledRepository(path.getParent().resolve(repositoryModel.path()), definitionReader);
        }
        return new DisabledRepository();
    }

    private Plugins plugins(
        FullSchematicHierarchy fullSchematicHierarchy,
        Properties properties,
        Preferences preferences,
        Repositories repositories,
        Dependencies dependencies
    ) {
        return new Plugins(
            fullSchematicHierarchy.plugins()
                .stream()
                .map(pluginModel -> new Plugin(pluginModel, properties, modelFactory, preferences, repositories))
                .collect(Collectors.toSet()),
            modulePathFactory,
            properties,
            dependencies
        );
    }

    private Properties properties(FullSchematicHierarchy fullSchematicHierarchy) {
        var properties = new HashMap<>(fullSchematicHierarchy.properties());
        properties.put(SchematicPropertyKey.NAME.fullName(), fullSchematicHierarchy.name());
        properties.put(SchematicPropertyKey.VERSION.fullName(), String.valueOf(fullSchematicHierarchy.version()));
        put(
            properties,
            SchematicPropertyKey.DISCOVERY_DIRECTORY,
            fullSchematicHierarchy.path(),
            ""
        );
        put(
            properties,
            SchematicPropertyKey.CONSTRUCTION_DIRECTORY,
            fullSchematicHierarchy.path(),
            ".conveyor"
        );
        return new Properties(properties);
    }

    private Preferences preferences(
        FullSchematicHierarchy fullSchematicHierarchy,
        Repositories repositories,
        Properties properties
    ) {
        return new Preferences(
            artifactPreferenceModels(fullSchematicHierarchy.preferences(), repositories, properties)
                .collect(
                    Collectors.toMap(
                        ArtifactPreferenceModel::name,
                        artifactPreferenceModel ->
                            new SemanticVersion(properties.interpolated(artifactPreferenceModel.version()))
                    )
                )
        );
    }

    private Stream<ArtifactPreferenceModel> artifactPreferenceModels(
        PreferencesModel preferencesModel,
        Repositories repositories,
        Properties properties
    ) {
        return Stream.concat(
            preferencesModel.inclusions()
                .stream()
                .map(preferencesInclusionModel ->
                    modelFactory.manualHierarchy(
                        preferencesInclusionModel.name(),
                        new SemanticVersion(properties.interpolated(preferencesInclusionModel.version())),
                        repositories
                    )
                )
                .map(Hierarchy::preferences)
                .flatMap(model -> artifactPreferenceModels(model, repositories, properties)),
            preferencesModel.artifacts()
                .stream()
        );
    }

    private void put(
        Map<String, String> properties,
        SchematicPropertyKey schematicPropertyKey,
        Path path,
        String defaultRelativePath
    ) {
        properties.put(
            schematicPropertyKey.fullName(),
            path.getParent()
                .resolve(properties.getOrDefault(schematicPropertyKey.fullName(), defaultRelativePath))
                .normalize()
                .toString()
        );
    }
}
