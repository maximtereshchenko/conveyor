package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionTranslator;
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
    private final DefinitionTranslator definitionTranslator;
    private final ModelFactory modelFactory;
    private final ModulePathFactory modulePathFactory;
    private final XmlFactory xmlFactory;
    private final Http http;

    Schematic(
        PartialSchematicHierarchy partialSchematicHierarchy,
        DefinitionTranslator definitionTranslator,
        ModelFactory modelFactory,
        ModulePathFactory modulePathFactory,
        XmlFactory xmlFactory, Http http
    ) {
        this.partialSchematicHierarchy = partialSchematicHierarchy;
        this.definitionTranslator = definitionTranslator;
        this.modelFactory = modelFactory;
        this.modulePathFactory = modulePathFactory;
        this.xmlFactory = xmlFactory;
        this.http = http;
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
        var repositories = repositories(properties(partialSchematicHierarchy));
        var fullSchematicHierarchy = modelFactory.fullSchematicHierarchy(
            partialSchematicHierarchy,
            repositories
        );
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
                    dependencies(
                        fullSchematicHierarchy,
                        properties,
                        preferences,
                        repositories,
                        schematicProducts
                    )
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
                    dependency(
                        dependencyModel,
                        properties,
                        preferences,
                        repositories,
                        schematicProducts
                    )
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
            case SchematicDependencyModel model -> new SchematicDependency(
                model,
                schematicProducts,
                modelFactory,
                repositories,
                preferences,
                properties
            );
        };
    }

    private Optional<SchematicDependencyModel> schematicDependencyModel(DependencyModel dependencyModel) {
        if (dependencyModel instanceof SchematicDependencyModel model) {
            return Optional.of(model);
        }
        return Optional.empty();
    }

    private Repositories repositories(Properties properties) {
        return new Repositories(
            partialSchematicHierarchy.repositories()
                .stream()
                .map(repositoryModel ->
                    repository(
                        repositoryModel,
                        partialSchematicHierarchy.path(),
                        properties
                    )
                )
                .collect(Collectors.toSet()),
            definitionTranslator
        );
    }

    private Repository repository(
        RepositoryModel repositoryModel,
        Path path,
        Properties properties
    ) {
        if (!repositoryModel.enabled().orElse(Boolean.TRUE)) {
            return new DisabledRepository();
        }
        return switch (repositoryModel) {
            case LocalDirectoryRepositoryModel model -> new LocalDirectoryRepository(
                absolutePath(path.getParent(), model.path())
            );
            case RemoteRepositoryModel model -> new RemoteRepository(
                model.url(),
                http,
                xmlFactory,
                definitionTranslator,
                new LocalDirectoryRepository(
                    absolutePath(path.getParent(), properties.remoteRepositoryCacheDirectory())
                )
            );
        };
    }

    private Path absolutePath(Path base, Path relative) {
        return base.resolve(relative).normalize();
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
                .map(pluginModel ->
                    new Plugin(
                        pluginModel,
                        properties,
                        modelFactory,
                        preferences,
                        repositories
                    )
                )
                .collect(Collectors.toSet()),
            modulePathFactory,
            properties,
            dependencies
        );
    }

    private <T extends TemplateModel, R extends TemplateModel> Properties properties(
        SchematicHierarchy<T, R> schematicHierarchy
    ) {
        var properties = new HashMap<>(schematicHierarchy.properties());
        properties.put(
            ConveyorPropertyKey.SCHEMATIC_NAME.fullName(),
            schematicHierarchy.name()
        );
        properties.put(
            ConveyorPropertyKey.SCHEMATIC_VERSION.fullName(),
            String.valueOf(schematicHierarchy.version())
        );
        put(
            properties,
            ConveyorPropertyKey.DISCOVERY_DIRECTORY,
            schematicHierarchy.path(),
            ""
        );
        put(
            properties,
            ConveyorPropertyKey.CONSTRUCTION_DIRECTORY,
            schematicHierarchy.path(),
            ".conveyor"
        );
        put(
            properties,
            ConveyorPropertyKey.REMOTE_REPOSITORY_CACHE_DIRECTORY,
            partialSchematicHierarchy.rootPath(),
            ".conveyor-modules"
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
                        artifactPreferenceModel ->
                            artifactPreferenceModel.group() + ':' + artifactPreferenceModel.name(),
                        artifactPreferenceModel ->
                            new SemanticVersion(
                                properties.interpolated(artifactPreferenceModel.version())
                            )
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
                        preferencesInclusionModel.group(),
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
        ConveyorPropertyKey schematicPropertyKey,
        Path path,
        String defaultRelativePath
    ) {
        properties.put(
            schematicPropertyKey.fullName(),
            path.getParent()
                .resolve(
                    properties.getOrDefault(
                        schematicPropertyKey.fullName(),
                        defaultRelativePath
                    )
                )
                .normalize()
                .toString()
        );
    }
}
