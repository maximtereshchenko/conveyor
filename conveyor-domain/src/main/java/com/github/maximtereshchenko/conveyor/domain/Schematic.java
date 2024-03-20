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
        var preferences = preferences(fullSchematicHierarchy);
        return schematicProducts
            .with(
                fullSchematicHierarchy.name(),
                plugins(
                    fullSchematicHierarchy,
                    properties(fullSchematicHierarchy),
                    preferences,
                    repositories,
                    dependencies(fullSchematicHierarchy, preferences, repositories, schematicProducts)
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
        Preferences preferences,
        Repositories repositories,
        SchematicProducts schematicProducts
    ) {
        return new Dependencies(
            fullSchematicHierarchy.dependencies()
                .stream()
                .map(dependencyModel -> dependency(dependencyModel, preferences, repositories, schematicProducts))
                .collect(Collectors.toSet()),
            modulePathFactory
        );
    }

    private Dependency dependency(
        DependencyModel dependencyModel,
        Preferences preferences,
        Repositories repositories,
        SchematicProducts schematicProducts
    ) {
        return switch (dependencyModel) {
            case ArtifactDependencyModel model -> new DirectDependency(model, modelFactory, preferences, repositories);
            case SchematicDependencyModel model ->
                new SchematicDependency(model, schematicProducts, modelFactory, repositories, preferences);
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

    private Preferences preferences(FullSchematicHierarchy fullSchematicHierarchy) {
        return new Preferences(
            fullSchematicHierarchy.preferences()
                .artifacts()
                .stream()
                .collect(Collectors.toMap(ArtifactPreferenceModel::name, ArtifactPreferenceModel::version))
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
