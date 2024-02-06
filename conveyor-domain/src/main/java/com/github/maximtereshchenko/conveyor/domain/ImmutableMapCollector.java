package com.github.maximtereshchenko.conveyor.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class ImmutableMapCollector<T, K> implements Collector<T, Map<K, T>, ImmutableMap<K, T>> {

    private final Function<T, K> classifier;

    ImmutableMapCollector(Function<T, K> classifier) {
        this.classifier = classifier;
    }

    @Override
    public Supplier<Map<K, T>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<K, T>, T> accumulator() {
        return (map, value) -> map.put(classifier.apply(value), value);
    }

    @Override
    public BinaryOperator<Map<K, T>> combiner() {
        return (first, second) -> {
            first.putAll(second);
            return first;
        };
    }

    @Override
    public Function<Map<K, T>, ImmutableMap<K, T>> finisher() {
        return ImmutableMap::new;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
