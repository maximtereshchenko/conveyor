package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Schematic {

    private final ExtendableLocalInheritanceHierarchyModel localModel;
    private final ModulePathFactory modulePathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final SchematicModelFactory schematicModelFactory;

    Schematic(
        ExtendableLocalInheritanceHierarchyModel localModel,
        ModulePathFactory modulePathFactory,
        PomDefinitionFactory pomDefinitionFactory,
        SchematicDefinitionConverter schematicDefinitionConverter,
        SchematicModelFactory schematicModelFactory
    ) {
        this.localModel = localModel;
        this.modulePathFactory = modulePathFactory;
        this.pomDefinitionFactory = pomDefinitionFactory;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.schematicModelFactory = schematicModelFactory;
    }

    Id id() {
        return localModel.id();
    }

    boolean locatedAt(Path path) {
        return localModel.path().equals(path);
    }

    boolean inheritsFrom(Schematic schematic) {
        return localModel.inheritsFrom(schematic.localModel);
    }

    boolean dependsOn(Schematic schematic, Schematics schematics) {
        return localModel.dependencies()
            .stream()
            .anyMatch(dependencyModel ->
                dependencyModel.id().equals(schematic.id()) ||
                dependencyExistsBetweenSchematics(dependencyModel.id(), schematic, schematics)
            );
    }

    Set<Product> construct(Set<Product> products, Stage stage) {
        var repositories = repositories(products, properties(localModel));
        var completeModel = schematicModelFactory.completeInheritanceHierarchyModel(
            localModel,
            repositories
        );
        var properties = properties(completeModel);
        var preferences = preferences(completeModel, properties, repositories);
        var schematicCoordinates = completeModel.id().coordinates(completeModel.version());
        var dependencies = dependencies(completeModel, properties, preferences, repositories);
        return plugins(completeModel, properties, preferences, repositories)
            .executeTasks(
                new ConveyorSchematicAdapter(schematicCoordinates, properties, dependencies),
                withSchematicDefinition(products, schematicCoordinates, completeModel),
                stage
            );
    }

    private Set<Product> withSchematicDefinition(
        Set<Product> original,
        SchematicCoordinates schematicCoordinates,
        CompleteInheritanceHierarchyModel completeModel
    ) {
        var copy = new HashSet<>(original);
        copy.add(
            new Product(
                schematicCoordinates,
                completeModel.path(),
                ProductType.SCHEMATIC_DEFINITION
            )
        );
        return copy;
    }

    private boolean dependencyExistsBetweenSchematics(
        Id id,
        Schematic schematic,
        Schematics schematics
    ) {
        return schematics.schematic(id)
            .map(found -> found.dependsOn(schematic, schematics))
            .orElse(Boolean.FALSE);
    }

    private Dependencies dependencies(
        CompleteInheritanceHierarchyModel completeModel,
        Properties properties,
        Preferences preferences,
        Repositories repositories
    ) {
        return new Dependencies(
            completeModel.dependencies()
                .stream()
                .map(dependencyModel ->
                    new Dependency(
                        new DirectlyReferencedArtifact(
                            dependencyModel,
                            preferences,
                            properties,
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
                    localModel.repositories()
                        .stream()
                        .map(repositoryModel ->
                            repository(
                                repositoryModel,
                                localModel.path(),
                                properties
                            )
                        )
                )
                .collect(Collectors.toSet()),
            schematicDefinitionConverter
        );
    }

    private Repository<Path> repository(
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
            case RemoteRepositoryModel model -> remoteRepository(path, properties, model);
        };
    }

    private Repository<Path> remoteRepository(
        Path path,
        Properties properties,
        RemoteRepositoryModel remoteRepositoryModel
    ) {
        var cache = new LocalDirectoryRepository(
            absolutePath(path.getParent(), properties.remoteRepositoryCacheDirectory())
        );
        return new CachingRepository(
            new MavenRepositoryAdapter(
                new CachingRepository(
                    new RemoteMavenRepository(remoteRepositoryModel.uri()),
                    cache
                ),
                pomDefinitionFactory,
                schematicDefinitionConverter
            ),
            cache
        );
    }

    private Path absolutePath(Path base, Path relative) {
        return base.resolve(relative).normalize();
    }

    private Plugins plugins(
        CompleteInheritanceHierarchyModel completeModel,
        Properties properties,
        Preferences preferences,
        Repositories repositories
    ) {
        return new Plugins(
            completeModel.plugins()
                .stream()
                .map(pluginModel ->
                    new Plugin(
                        new DirectlyReferencedArtifact(
                            pluginModel,
                            preferences,
                            properties,
                            repositories,
                            schematicModelFactory
                        ),
                        pluginModel,
                        properties
                    )
                )
                .collect(Collectors.toCollection(LinkedHashSet::new)),
            modulePathFactory
        );
    }

    private <M extends LocalInheritanceHierarchyModel> Properties properties(
        M localInheritanceHierarchyModel
    ) {
        return new Properties(
            localInheritanceHierarchyModel.properties()
                .withResolvedPath(
                    SchematicPropertyKey.DISCOVERY_DIRECTORY,
                    localInheritanceHierarchyModel.path().getParent(),
                    ""
                )
                .withResolvedPath(
                    SchematicPropertyKey.CONSTRUCTION_DIRECTORY,
                    localInheritanceHierarchyModel.path().getParent(),
                    ".conveyor"
                )
                .withResolvedPath(
                    SchematicPropertyKey.REMOTE_REPOSITORY_CACHE_DIRECTORY,
                    localInheritanceHierarchyModel.rootPath().getParent(),
                    ".conveyor-modules"
                )
        );
    }

    private Map<Id, SemanticVersion> preferences(
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

    private Map<Id, SemanticVersion> artifactPreferences(
        Set<ArtifactPreferenceModel> artifacts,
        Properties properties
    ) {
        return artifacts.stream()
            .collect(
                Collectors.toMap(
                    ArtifactPreferenceModel::id,
                    artifactPreferenceModel -> new SemanticVersion(
                        properties.interpolated(artifactPreferenceModel.version())
                    )
                )
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
                    preferencesInclusionModel.id(),
                    new SemanticVersion(
                        properties.interpolated(preferencesInclusionModel.version())
                    ),
                    repositories
                )
            )
            .map(inheritanceHierarchyModel ->
                preferences(
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

    private Preferences preferences(
        CompleteInheritanceHierarchyModel completeModel,
        Properties properties,
        Repositories repositories
    ) {
        return new Preferences(preferences(completeModel.preferences(), properties, repositories));
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
}
