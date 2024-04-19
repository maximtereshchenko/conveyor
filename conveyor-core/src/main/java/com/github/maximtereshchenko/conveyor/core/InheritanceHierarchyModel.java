package com.github.maximtereshchenko.conveyor.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

record InheritanceHierarchyModel<T extends SchematicModel>(LinkedHashSet<T> models)
    implements SchematicModel,
    ExtendableInheritanceHierarchyModel<T, InheritanceHierarchyModel<T>> {

    @SafeVarargs
    InheritanceHierarchyModel(T... model) {
        this(new LinkedHashSet<>(List.of(model)));
    }

    @Override
    public Id id() {
        return models.getLast().id();
    }

    @Override
    public SemanticVersion version() {
        return models.getLast().version();
    }

    @Override
    public TemplateModel template() {
        return models.getFirst().template();
    }

    @Override
    public PropertiesModel properties() {
        return models.stream()
            .map(SchematicModel::properties)
            .reduce((previous, next) -> next.override(previous))
            .orElseThrow();
    }

    @Override
    public PreferencesModel preferences() {
        return models.stream()
            .map(SchematicModel::preferences)
            .reduce((first, second) -> second.override(first))
            .orElseThrow();
    }

    @Override
    public LinkedHashSet<PluginModel> plugins() {
        return combine(SchematicModel::plugins, PluginModel::id, PluginModel::override);
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return combine(
            SchematicModel::dependencies,
            DependencyModel::id,
            DependencyModel::override
        );
    }

    @Override
    public InheritanceHierarchyModel<T> inheritedFrom(T model) {
        var copy = new LinkedHashSet<>(models);
        copy.addFirst(model);
        return new InheritanceHierarchyModel<>(copy);
    }

    @Override
    public boolean inheritsFrom(InheritanceHierarchyModel<T> inheritanceHierarchyModel) {
        return models.containsAll(inheritanceHierarchyModel.models);
    }

    private <O, I> LinkedHashSet<O> combine(
        Function<T, Set<O>> extractor,
        Function<O, I> classifier,
        BinaryOperator<O> combiner
    ) {
        return models.stream()
            .map(extractor)
            .flatMap(Collection::stream)
            .collect(new ReducingCollector<>(classifier, combiner));
    }
}
