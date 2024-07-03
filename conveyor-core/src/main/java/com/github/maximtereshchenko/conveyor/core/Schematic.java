package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionConverter;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Schematic {

    private static final System.Logger LOGGER = System.getLogger(Schematic.class.getName());

    private final ExtendableLocalInheritanceHierarchyModel localModel;
    private final ClasspathFactory classpathFactory;
    private final PomDefinitionFactory pomDefinitionFactory;
    private final SchematicDefinitionConverter schematicDefinitionConverter;
    private final SchematicModelFactory schematicModelFactory;
    private final PreferencesFactory preferencesFactory;

    Schematic(
        ExtendableLocalInheritanceHierarchyModel localModel,
        ClasspathFactory classpathFactory,
        PomDefinitionFactory pomDefinitionFactory,
        SchematicDefinitionConverter schematicDefinitionConverter,
        SchematicModelFactory schematicModelFactory,
        PreferencesFactory preferencesFactory
    ) {
        this.localModel = localModel;
        this.classpathFactory = classpathFactory;
        this.pomDefinitionFactory = pomDefinitionFactory;
        this.schematicDefinitionConverter = schematicDefinitionConverter;
        this.schematicModelFactory = schematicModelFactory;
        this.preferencesFactory = preferencesFactory;
    }

    Id id() {
        return localModel.id();
    }

    Version version() {
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

    ConstructionRepository construct(
        ConstructionRepository constructionRepository,
        List<Stage> stages
    ) {
        LOGGER.log(
            System.Logger.Level.INFO,
            () -> "Constructing %s:%s:%s".formatted(
                localModel.id().group(),
                localModel.id().name(),
                localModel.version()
            )
        );
        var updatedConstructionRepository = constructionRepository.withSchematicDefinition(
            localModel.id(),
            localModel.version(),
            localModel.path()
        );
        var repositories = repositories(updatedConstructionRepository, properties(localModel));
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
            completeModel.path(),
            completeModel.id(),
            completeModel.version(),
            properties,
            dependencies,
            repositories
        );
        return plugins(completeModel, properties, preferences, repositories)
            .executeTasks(
                conveyorSchematic,
                properties,
                stages
            )
            .map(path ->
                updatedConstructionRepository.withArtifact(
                    completeModel.id(),
                    completeModel.version(),
                    path
                )
            )
            .orElse(updatedConstructionRepository);
    }

    private boolean dependencyExists(
        Schematic schematic,
        Schematics schematics,
        Id id
    ) {
        return schematic.id().equals(id) ||
               dependencyExistsBetweenSchematics(id, schematic, schematics);
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
            classpathFactory
        );
    }

    private Repositories repositories(
        ConstructionRepository constructionRepository,
        Properties properties
    ) {
        return new Repositories(
            Stream.concat(
                    Stream.of(constructionRepository),
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
                .collect(Collectors.toCollection(LinkedHashSet::new)),
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
            classpathFactory
        );
    }

    private <M extends LocalInheritanceHierarchyModel> Properties properties(
        M localInheritanceHierarchyModel
    ) {
        var conveyorCache = localInheritanceHierarchyModel.rootPath()
            .getParent()
            .resolve(".conveyor-cache");
        return new Properties(
            localInheritanceHierarchyModel.properties()
                .withResolvedPath(
                    SchematicPropertyKey.REMOTE_REPOSITORY_CACHE_DIRECTORY,
                    localInheritanceHierarchyModel.rootPath().getParent(),
                    conveyorCache.resolve("repository")
                )
                .withResolvedPath(
                    SchematicPropertyKey.TASKS_CACHE_DIRECTORY,
                    localInheritanceHierarchyModel.path().getParent(),
                    localInheritanceHierarchyModel.id().path(conveyorCache.resolve("tasks"))
                )
        );
    }
}
