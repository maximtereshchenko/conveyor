package com.github.maximtereshchenko.conveyor.domain;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class Hierarchy<T extends TemplateModel, D extends DependencyModel, M extends Model<? extends TemplateModel,
    D>>
    implements Model<T, D> {

    private final LinkedHashSet<? extends M> models;

    Hierarchy(LinkedHashSet<? extends M> models) {
        this.models = models;
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
            .map(Model::preferences)
            .reduce((first, second) -> second.override(first))
            .orElseThrow();
    }

    @Override
    public Set<PluginModel> plugins() {
        return reduce(Model::plugins, PluginModel::name, PluginModel::override);
    }

    LinkedHashSet<M> models() {
        return new LinkedHashSet<>(models);
    }

    boolean inheritsFrom(Hierarchy<T, D, M> hierarchy) {
        return models.containsAll(hierarchy.models());
    }

    <O> Set<O> reduce(
        Function<M, Set<O>> extractor,
        Function<O, String> classifier,
        BinaryOperator<O> reducer
    ) {
        return Set.copyOf(
            models.stream()
                .map(extractor)
                .flatMap(Collection::stream)
                .collect(
                    Collectors.toMap(classifier, Function.identity(), (first, second) -> reducer.apply(second, first))
                )
                .values()
        );
    }
}
