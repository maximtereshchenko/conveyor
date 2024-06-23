package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.LocalDirectoryRepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.RemoteRepositoryDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class StandaloneLocalSchematicModel implements LocalSchematicModel {

    private static final String DEFAULT_SCHEMATIC_DEFINITION_NAME = "conveyor.json";

    private final Path path;
    private final StandaloneSchematicModel standaloneSchematicModel;

    StandaloneLocalSchematicModel(Path path, StandaloneSchematicModel standaloneSchematicModel) {
        this.path = path;
        this.standaloneSchematicModel = standaloneSchematicModel;
    }

    @Override
    public Id id() {
        return standaloneSchematicModel.id();
    }

    @Override
    public Version version() {
        return standaloneSchematicModel.version();
    }

    @Override
    public TemplateModel template() {
        return standaloneSchematicModel.template();
    }

    @Override
    public PropertiesModel properties() {
        return standaloneSchematicModel.properties()
            .withResolvedPath(
                SchematicPropertyKey.TEMPLATE_LOCATION,
                path.getParent(),
                "../" + DEFAULT_SCHEMATIC_DEFINITION_NAME
            )
            .with(SchematicPropertyKey.SCHEMATIC_DIRECTORY, path.getParent().toString());
    }

    @Override
    public PreferencesModel preferences() {
        return standaloneSchematicModel.preferences();
    }

    @Override
    public LinkedHashSet<PluginModel> plugins() {
        return standaloneSchematicModel.plugins();
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return standaloneSchematicModel.dependencies();
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public Path templatePath() {
        return properties().path(SchematicPropertyKey.TEMPLATE_LOCATION);
    }

    @Override
    public LinkedHashSet<Path> inclusions() {
        return standaloneSchematicModel.schematicDefinition()
            .inclusions()
            .stream()
            .map(path.getParent()::resolve)
            .map(Path::normalize)
            .map(this::schematicDefinitionPath)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public LinkedHashSet<RepositoryModel> repositories() {
        return standaloneSchematicModel.schematicDefinition()
            .repositories()
            .stream()
            .map(repositoryDefinition ->
                switch (repositoryDefinition) {
                    case LocalDirectoryRepositoryDefinition definition ->
                        new LocalDirectoryRepositoryModel(
                            definition.name(),
                            path.getParent().resolve(definition.path()).normalize()
                        );
                    case RemoteRepositoryDefinition definition -> new RemoteRepositoryModel(
                        definition.name(),
                        definition.uri()
                    );
                }
            )
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public int hashCode() {
        return Objects.hash(standaloneSchematicModel, path);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (StandaloneLocalSchematicModel) object;
        return Objects.equals(standaloneSchematicModel, that.standaloneSchematicModel) &&
               Objects.equals(path, that.path);
    }

    private Path schematicDefinitionPath(Path potentialDirectory) {
        if (Files.isDirectory(potentialDirectory)) {
            return potentialDirectory.resolve(DEFAULT_SCHEMATIC_DEFINITION_NAME);
        }
        return potentialDirectory;
    }
}
