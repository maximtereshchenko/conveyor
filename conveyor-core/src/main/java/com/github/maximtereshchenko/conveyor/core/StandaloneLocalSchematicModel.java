package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.LocalDirectoryRepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.schematic.RemoteRepositoryDefinition;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class StandaloneLocalSchematicModel implements LocalSchematicModel {

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
    public SemanticVersion version() {
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
                "../conveyor.json"
            );
    }

    @Override
    public PreferencesModel preferences() {
        return standaloneSchematicModel.preferences();
    }

    @Override
    public Set<PluginModel> plugins() {
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
        return new LinkedHashSet<>(standaloneSchematicModel.schematicDefinition().inclusions());
    }

    @Override
    public Set<RepositoryModel> repositories() {
        return standaloneSchematicModel.schematicDefinition()
            .repositories()
            .stream()
            .map(repositoryDefinition ->
                switch (repositoryDefinition) {
                    case LocalDirectoryRepositoryDefinition definition ->
                        new LocalDirectoryRepositoryModel(
                            definition.name(),
                            definition.path(),
                            definition.enabled()
                        );
                    case RemoteRepositoryDefinition definition -> new RemoteRepositoryModel(
                        definition.name(),
                        definition.url(),
                        definition.enabled()
                    );
                }
            )
            .collect(Collectors.toSet());
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
}
