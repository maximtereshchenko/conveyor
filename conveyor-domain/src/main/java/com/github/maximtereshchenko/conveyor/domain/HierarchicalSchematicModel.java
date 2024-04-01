package com.github.maximtereshchenko.conveyor.domain;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

final class HierarchicalSchematicModel<T extends SchematicModel> implements SchematicModel {

    private final LinkedHashSet<T> models;

    HierarchicalSchematicModel(LinkedHashSet<T> models) {
        this.models = models;
    }

    @SafeVarargs
    HierarchicalSchematicModel(T... model) {
        this(new LinkedHashSet<>(List.of(model)));
    }

    @Override
    public String group() {
        return models.getLast().group();
    }

    @Override
    public String name() {
        return models.getLast().name();
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
    public Map<String, String> properties() {
        var properties = new HashMap<String, String>();
        for (var model : models) {
            properties.putAll(model.properties());
        }
        return properties;
    }

    @Override
    public PreferencesModel preferences() {
        return models.stream()
            .map(SchematicModel::preferences)
            .reduce((first, second) -> second.override(first))
            .orElseThrow();
    }

    @Override
    public Set<PluginModel> plugins() {
        return reduce(SchematicModel::plugins, PluginModel::name, PluginModel::override);
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return reduce(
            SchematicModel::dependencies,
            DependencyModel::name,
            DependencyModel::override
        );
    }

    LinkedHashSet<T> models() {
        return new LinkedHashSet<>(models);
    }

    <O> Set<O> reduce(
        Function<T, Set<O>> extractor,
        Function<O, String> classifier,
        BinaryOperator<O> reducer
    ) {
        return Set.copyOf(
            models.stream()
                .map(extractor)
                .flatMap(Collection::stream)
                .collect(
                    Collectors.toMap(
                        classifier,
                        Function.identity(),
                        (first, second) -> reducer.apply(second, first)
                    )
                )
                .values()
        );
    }

    HierarchicalSchematicModel<T> inheritedFrom(T model) {
        var copy = new LinkedHashSet<>(models);
        copy.addFirst(model);
        return new HierarchicalSchematicModel<>(copy);
    }

    boolean inheritsFrom(HierarchicalSchematicModel<T> hierarchicalSchematicModel) {
        return models.containsAll(hierarchicalSchematicModel.models);
    }
}
