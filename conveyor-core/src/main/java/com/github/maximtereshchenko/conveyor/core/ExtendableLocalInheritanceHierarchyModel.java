package com.github.maximtereshchenko.conveyor.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

final class ExtendableLocalInheritanceHierarchyModel
    implements LocalInheritanceHierarchyModel,
    ExtendableInheritanceHierarchyModel<StandaloneLocalSchematicModel,
        ExtendableLocalInheritanceHierarchyModel> {

    private final InheritanceHierarchyModel<LocalSchematicModel> original;

    ExtendableLocalInheritanceHierarchyModel(
        InheritanceHierarchyModel<LocalSchematicModel> original
    ) {
        this.original = original;
    }

    @Override
    public Id id() {
        return original.id();
    }

    @Override
    public SemanticVersion version() {
        return original.version();
    }

    @Override
    public TemplateModel template() {
        return original.template();
    }

    @Override
    public PropertiesModel properties() {
        return original.properties();
    }

    @Override
    public PreferencesModel preferences() {
        return original.preferences();
    }

    @Override
    public Set<PluginModel> plugins() {
        return original.plugins();
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return original.dependencies();
    }

    @Override
    public Path path() {
        return original.models().getLast().path();
    }

    @Override
    public Path templatePath() {
        return original.models().getFirst().templatePath();
    }

    @Override
    public LinkedHashSet<Path> inclusions() {
        return original.models().getLast().inclusions();
    }

    @Override
    public Set<RepositoryModel> repositories() {
        return original.models()
            .stream()
            .map(LocalSchematicModel::repositories)
            .flatMap(Collection::stream)
            .collect(new OverridingCollector<>(RepositoryModel::name, RepositoryModel::override));
    }

    @Override
    public Path rootPath() {
        return original.models().getFirst().path();
    }

    @Override
    public ExtendableLocalInheritanceHierarchyModel inheritedFrom(
        StandaloneLocalSchematicModel standaloneLocalSchematicModel
    ) {
        return new ExtendableLocalInheritanceHierarchyModel(
            original.inheritedFrom(standaloneLocalSchematicModel)
        );
    }

    @Override
    public boolean inheritsFrom(
        ExtendableLocalInheritanceHierarchyModel extendableLocalInheritanceHierarchyModel
    ) {
        return original.inheritsFrom(extendableLocalInheritanceHierarchyModel.original);
    }
}
