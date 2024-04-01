package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.LocalDirectoryRepositoryDefinition;
import com.github.maximtereshchenko.conveyor.api.port.RemoteRepositoryDefinition;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class StandaloneLocalSchematicModel implements LocalSchematicModel {

    private final StandaloneSchematicModel standaloneSchematicModel;
    private final Path path;

    StandaloneLocalSchematicModel(
        StandaloneSchematicModel standaloneSchematicModel,
        Path path
    ) {
        this.standaloneSchematicModel = standaloneSchematicModel;
        this.path = path;
    }

    @Override
    public String group() {
        return standaloneSchematicModel.group();
    }

    @Override
    public String name() {
        return standaloneSchematicModel.name();
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
    public Map<String, String> properties() {
        return standaloneSchematicModel.properties();
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
        return path.getParent()
            .resolve(
                standaloneSchematicModel.properties()
                    .get(SchematicPropertyKey.TEMPLATE_LOCATION.fullName())
            )
            .normalize();
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
