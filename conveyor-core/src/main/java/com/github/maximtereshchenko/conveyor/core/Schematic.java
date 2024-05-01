package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.SchematicCoordinates;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Schematic {

    private static final System.Logger LOGGER = System.getLogger(Schematic.class.getName());

    private final ExtendableLocalInheritanceHierarchyModel localModel;
    private final ClassPathFactory classPathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final SchematicModelFactory schematicModelFactory;
    private final PreferencesFactory preferencesFactory;

    Schematic(
        ExtendableLocalInheritanceHierarchyModel localModel,
        ClassPathFactory classPathFactory,
        PomDefinitionFactory pomDefinitionFactory,
        SchematicDefinitionConverter schematicDefinitionConverter,
        SchematicModelFactory schematicModelFactory,
        PreferencesFactory preferencesFactory
    ) {
        this.localModel = localModel;
        this.classPathFactory = classPathFactory;
        this.pomDefinitionFactory = pomDefinitionFactory;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.schematicModelFactory = schematicModelFactory;
        this.preferencesFactory = preferencesFactory;
    }

    Id id() {
        return localModel.id();
    }

    SemanticVersion version() {
        return localModel.version();
    }

    boolean locatedAt(Path path) {
        return localModel.path().equals(path);
    }

    boolean inheritsFrom(Schematic schematic) {
        return localModel.inheritsFrom(schematic.localModel);
    }

    boolean dependsOn(Schematic schematic, Schematics schematics) {
        var properties = properties(localModel);
        return localModel.dependencies()
            .stream()
            .anyMatch(dependencyModel ->
                dependencyExists(schematic, schematics, dependencyModel.idModel().id(properties))
            );
    }

    Set<Product> construct(Set<Product> products, Stage stage) {
        LOGGER.log(
            System.Logger.Level.INFO,
            () -> "Constructing %s:%s:%s".formatted(
                localModel.id().group(),
                localModel.id().name(),
                localModel.version()
            )
        );
        var repositories = repositories(products, properties(localModel));
        var completeModel = schematicModelFactory.completeInheritanceHierarchyModel(
            localModel,
            repositories
        );
        var properties = properties(completeModel);
        var preferences = preferencesFactory.preferences(
            completeModel.preferences(),
            properties,
            repositories
        );
        var dependencies = dependencies(completeModel, properties, preferences, repositories);
        var conveyorSchematic = new ConveyorSchematicAdapter(
            completeModel.id(),
            completeModel.version(),
            properties,
            dependencies,
            repositories
        );
        return plugins(completeModel, properties, preferences, repositories)
            .executeTasks(
                conveyorSchematic,
                withSchematicDefinition(products, conveyorSchematic.coordinates(), completeModel),
                stage
            );
    }

    private boolean dependencyExists(
        Schematic schematic,
        Schematics schematics,
        Id id
    ) {
        return schematic.id().equals(id) ||
               dependencyExistsBetweenSchematics(id, schematic, schematics);
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
                            schematicModelFactory,
                            preferencesFactory
                        ),
                        dependencyModel
                    )
                )
                .collect(Collectors.toSet()),
            classPathFactory
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
        return switch (repositoryModel) {
            case LocalDirectoryRepositoryModel model -> new NamedLocalDirectoryRepository(
                new LocalDirectoryRepository(
                    absolutePath(path.getParent(), model.path())
                ),
                model.name()
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
                    new RemoteMavenRepository(
                        remoteRepositoryModel.name(),
                        remoteRepositoryModel.uri()
                    ),
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
                            schematicModelFactory,
                            preferencesFactory
                        ),
                        pluginModel,
                        properties
                    )
                )
                .collect(Collectors.toCollection(LinkedHashSet::new)),
            classPathFactory
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
                    ".conveyor-cache"
                )
        );
    }
}
