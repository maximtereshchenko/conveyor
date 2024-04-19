package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

final class CompleteInheritanceHierarchyModel
    implements LocalSchematicModel, LocalInheritanceHierarchyModel {

    private final InheritanceHierarchyModel<SchematicModel> hierarchyModel;
    private final ExtendableLocalInheritanceHierarchyModel localModel;

    CompleteInheritanceHierarchyModel(
        InheritanceHierarchyModel<SchematicModel> remoteModel,
        ExtendableLocalInheritanceHierarchyModel localModel
    ) {
        this.hierarchyModel = new InheritanceHierarchyModel<>(remoteModel, localModel);
        this.localModel = localModel;
    }

    CompleteInheritanceHierarchyModel(ExtendableLocalInheritanceHierarchyModel localModel) {
        this.hierarchyModel = new InheritanceHierarchyModel<>(
            localModel
        );
        this.localModel = localModel;
    }

    @Override
    public Id id() {
        return hierarchyModel.id();
    }

    @Override
    public SemanticVersion version() {
        return hierarchyModel.version();
    }

    @Override
    public TemplateModel template() {
        return hierarchyModel.template();
    }

    @Override
    public PropertiesModel properties() {
        return hierarchyModel.properties();
    }

    @Override
    public PreferencesModel preferences() {
        return hierarchyModel.preferences();
    }

    @Override
    public LinkedHashSet<PluginModel> plugins() {
        return hierarchyModel.plugins();
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return hierarchyModel.dependencies();
    }

    @Override
    public Path rootPath() {
        return localModel.rootPath();
    }

    @Override
    public Path path() {
        return localModel.path();
    }

    @Override
    public Path templatePath() {
        return localModel.templatePath();
    }

    @Override
    public LinkedHashSet<Path> inclusions() {
        return localModel.inclusions();
    }

    @Override
    public LinkedHashSet<RepositoryModel> repositories() {
        return localModel.repositories();
    }
}
