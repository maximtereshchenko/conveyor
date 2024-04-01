package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class HierarchicalLocalSchematicModel
    implements LocalSchematicModel, LocalSchematicHierarchyModel {

    private final HierarchicalSchematicModel<LocalSchematicModel> hierarchicalSchematicModel;

    HierarchicalLocalSchematicModel(
        HierarchicalSchematicModel<LocalSchematicModel> hierarchicalSchematicModel
    ) {
        this.hierarchicalSchematicModel = hierarchicalSchematicModel;
    }

    @Override
    public String group() {
        return hierarchicalSchematicModel.group();
    }

    @Override
    public String name() {
        return hierarchicalSchematicModel.name();
    }

    @Override
    public SemanticVersion version() {
        return hierarchicalSchematicModel.version();
    }

    @Override
    public TemplateModel template() {
        return hierarchicalSchematicModel.template();
    }

    @Override
    public Map<String, String> properties() {
        return hierarchicalSchematicModel.properties();
    }

    @Override
    public PreferencesModel preferences() {
        return hierarchicalSchematicModel.preferences();
    }

    @Override
    public Set<PluginModel> plugins() {
        return hierarchicalSchematicModel.plugins();
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return hierarchicalSchematicModel.dependencies();
    }

    @Override
    public Path path() {
        return hierarchicalSchematicModel.models().getLast().path();
    }

    @Override
    public Path templatePath() {
        return hierarchicalSchematicModel.models().getFirst().templatePath();
    }

    @Override
    public LinkedHashSet<Path> inclusions() {
        return hierarchicalSchematicModel.models().getLast().inclusions();
    }

    @Override
    public Set<RepositoryModel> repositories() {
        return hierarchicalSchematicModel.reduce(
            LocalSchematicModel::repositories,
            RepositoryModel::name,
            RepositoryModel::override
        );
    }

    @Override
    public Path rootPath() {
        return hierarchicalSchematicModel.models().getFirst().path();
    }

    HierarchicalLocalSchematicModel inheritedFrom(
        StandaloneLocalSchematicModel standaloneLocalSchematicModel
    ) {
        return new HierarchicalLocalSchematicModel(
            hierarchicalSchematicModel.inheritedFrom(standaloneLocalSchematicModel)
        );
    }

    boolean inheritsFrom(HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel) {
        return hierarchicalSchematicModel.inheritsFrom(
            hierarchicalLocalSchematicModel.hierarchicalSchematicModel
        );
    }
}
