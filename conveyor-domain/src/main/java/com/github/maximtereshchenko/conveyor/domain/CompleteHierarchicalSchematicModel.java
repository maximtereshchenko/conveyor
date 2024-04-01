package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class CompleteHierarchicalSchematicModel
    implements LocalSchematicModel, LocalSchematicHierarchyModel {

    private final HierarchicalSchematicModel<SchematicModel> hierarchicalSchematicModel;
    private final HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel;

    CompleteHierarchicalSchematicModel(
        HierarchicalSchematicModel<SchematicModel> hierarchicalSchematicModel,
        HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel
    ) {
        this.hierarchicalSchematicModel = new HierarchicalSchematicModel<>(
            hierarchicalSchematicModel,
            hierarchicalLocalSchematicModel
        );
        this.hierarchicalLocalSchematicModel = hierarchicalLocalSchematicModel;
    }

    CompleteHierarchicalSchematicModel(
        HierarchicalLocalSchematicModel hierarchicalLocalSchematicModel
    ) {
        this.hierarchicalSchematicModel = new HierarchicalSchematicModel<>(
            hierarchicalLocalSchematicModel
        );
        this.hierarchicalLocalSchematicModel = hierarchicalLocalSchematicModel;
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
    public Path rootPath() {
        return hierarchicalLocalSchematicModel.rootPath();
    }

    @Override
    public Path path() {
        return hierarchicalLocalSchematicModel.path();
    }

    @Override
    public Path templatePath() {
        return hierarchicalLocalSchematicModel.templatePath();
    }

    @Override
    public LinkedHashSet<Path> inclusions() {
        return hierarchicalLocalSchematicModel.inclusions();
    }

    @Override
    public Set<RepositoryModel> repositories() {
        return hierarchicalLocalSchematicModel.repositories();
    }
}
