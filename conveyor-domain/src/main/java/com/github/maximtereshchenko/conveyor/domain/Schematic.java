package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionTranslator;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Schematic {

    private final HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel;
    private final SchematicDefinitionTranslator schematicDefinitionTranslator;
    private final SchematicModelFactory schematicModelFactory;
    private final ModulePathFactory modulePathFactory;
    private final XmlFactory xmlFactory;
    private final Http http;

    Schematic(
        HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel,
        SchematicDefinitionTranslator schematicDefinitionTranslator,
        SchematicModelFactory schematicModelFactory,
        ModulePathFactory modulePathFactory,
        XmlFactory xmlFactory, Http http
    ) {
        this.hierarchicalLocalSchematicModel = hierarchicalLocalSchematicModel;
        this.schematicDefinitionTranslator = schematicDefinitionTranslator;
        this.schematicModelFactory = schematicModelFactory;
        this.modulePathFactory = modulePathFactory;
        this.xmlFactory = xmlFactory;
        this.http = http;
    }

    String group() {
        return hierarchicalLocalSchematicModel.group();
    }

    String name() {
        return hierarchicalLocalSchematicModel.name();
    }

    boolean locatedAt(Path path) {
        return hierarchicalLocalSchematicModel.path().equals(path);
    }

    boolean inheritsFrom(Schematic schematic) {
        return hierarchicalLocalSchematicModel.inheritsFrom(
            schematic.hierarchicalLocalSchematicModel
        );
    }

    boolean dependsOn(Schematic schematic, Schematics schematics) {
        return hierarchicalLocalSchematicModel.dependencies()
            .stream()
            .anyMatch(dependencyModel ->
                equalsByGroupAndName(schematic, dependencyModel) ||
                dependencyBetweenExists(schematic, schematics, dependencyModel)
            );
    }

    Set<Product> construct(Set<Product> products, Stage stage) {
        var repositories = repositories(products, properties(hierarchicalLocalSchematicModel));
        var completeHierarchicalSchematicModel =
            schematicModelFactory.completeHierarchicalSchematicModel(
                hierarchicalLocalSchematicModel,
                repositories
            );
        var properties = properties(completeHierarchicalSchematicModel);
        var preferences = preferences(completeHierarchicalSchematicModel, repositories, properties);
        var copy = new HashSet<>(products);
        copy.add(
            new Product(
                new SchematicCoordinates(
                    completeHierarchicalSchematicModel.group(),
                    completeHierarchicalSchematicModel.name(),
                    completeHierarchicalSchematicModel.version().toString()
                ),
                completeHierarchicalSchematicModel.path(),
                ProductType.SCHEMATIC_DEFINITION
            )
        );
        var dependencies = dependencies(
            completeHierarchicalSchematicModel,
            properties,
            preferences,
            repositories
        );
        return plugins(
            completeHierarchicalSchematicModel,
            properties,
            preferences,
            repositories
        )
            .executeTasks(
                new ConveyorSchematicAdapter(
                    new SchematicCoordinates(
                        completeHierarchicalSchematicModel.group(),
                        completeHierarchicalSchematicModel.name(),
                        completeHierarchicalSchematicModel.version().toString()
                    ),
                    properties,
                    dependencies
                ),
                copy,
                stage
            );
    }

    private boolean dependencyBetweenExists(
        Schematic schematic,
        Schematics schematics,
        DependencyModel dependencyModel
    ) {
        return schematics.schematic(dependencyModel.group(), dependencyModel.name())
            .map(found -> found.dependsOn(schematic, schematics))
            .orElse(Boolean.FALSE);
    }

    private boolean equalsByGroupAndName(Schematic schematic, DependencyModel dependencyModel) {
        return dependencyModel.group().equals(schematic.group()) &&
               dependencyModel.name().equals(schematic.name());
    }

    private Dependencies dependencies(
        CompleteHierarchicalSchematicModel completeHierarchicalSchematicModel,
        Properties properties,
        Preferences preferences,
        Repositories repositories
    ) {
        return new Dependencies(
            completeHierarchicalSchematicModel.dependencies()
                .stream()
                .map(dependencyModel ->
                    new Dependency(
                        new DirectlyReferencedArtifact(
                            dependencyModel,
                            properties,
                            preferences,
                            repositories,
                            schematicModelFactory
                        ),
                        dependencyModel
                    )
                )
                .collect(Collectors.toSet()),
            modulePathFactory
        );
    }

    private Repositories repositories(Set<Product> products, Properties properties) {
        return new Repositories(
            Stream.concat(
                    Stream.of(new ProductRepository(products)),
                    hierarchicalLocalSchematicModel.repositories()
                        .stream()
                        .map(repositoryModel ->
                            repository(
                                repositoryModel,
                                hierarchicalLocalSchematicModel.path(),
                                properties
                            )
                        )
                )
                .collect(Collectors.toSet()),
            schematicDefinitionTranslator
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
                schematicDefinitionTranslator,
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
        CompleteHierarchicalSchematicModel completeHierarchicalSchematicModel,
        Properties properties,
        Preferences preferences,
        Repositories repositories
    ) {
        return new Plugins(
            completeHierarchicalSchematicModel.plugins()
                .stream()
                .map(pluginModel ->
                    new Plugin(
                        new DirectlyReferencedArtifact(
                            pluginModel,
                            properties,
                            preferences,
                            repositories,
                            schematicModelFactory
                        ),
                        pluginModel,
                        properties
                    )
                )
                .collect(Collectors.toSet()),
            modulePathFactory
        );
    }

    private <M extends LocalSchematicModel & LocalSchematicHierarchyModel> Properties properties(
        M localSchematicModel
    ) {
        var properties = new HashMap<>(localSchematicModel.properties());
        put(
            properties,
            SchematicPropertyKey.DISCOVERY_DIRECTORY,
            localSchematicModel.path(),
            ""
        );
        put(
            properties,
            SchematicPropertyKey.CONSTRUCTION_DIRECTORY,
            localSchematicModel.path(),
            ".conveyor"
        );
        put(
            properties,
            SchematicPropertyKey.REMOTE_REPOSITORY_CACHE_DIRECTORY,
            localSchematicModel.rootPath(),
            ".conveyor-modules"
        );
        return new Properties(properties);
    }

    private Preferences preferences(
        CompleteHierarchicalSchematicModel completeHierarchicalSchematicModel,
        Repositories repositories,
        Properties properties
    ) {
        return new Preferences(
            artifactPreferenceModels(
                completeHierarchicalSchematicModel.preferences(),
                repositories,
                properties
            )
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
                    schematicModelFactory.hierarchicalSchematicModel(
                        preferencesInclusionModel.group(),
                        preferencesInclusionModel.name(),
                        new SemanticVersion(
                            properties.interpolated(preferencesInclusionModel.version())
                        ),
                        repositories
                    )
                )
                .map(HierarchicalSchematicModel::preferences)
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
