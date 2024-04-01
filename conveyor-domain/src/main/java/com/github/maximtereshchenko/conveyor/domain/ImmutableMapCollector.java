package com.github.maximtereshchenko.conveyor.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class ImmutableMapCollector<K, V> implements Collector<V, Map<K, V>, ImmutableMap<K, V>> {

    private final Function<V, K> classifier;
    private final BinaryOperator<V> merger;

    ImmutableMapCollector(Function<V, K> classifier, BinaryOperator<V> merger) {
        this.classifier = classifier;
        this.merger = merger;
    }

    ImmutableMapCollector(Function<V, K> classifier) {
        this(classifier, (present, inserted) -> {
            throw new IllegalArgumentException("Attempted merging values %s and %s".formatted(present, inserted));
        });
    }

    @Override
    public Supplier<Map<K, V>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<K, V>, V> accumulator() {
        return (map, value) ->
            map.compute(
                classifier.apply(value),
                (key, present) -> present == null ? value : merger.apply(present, value)
            );
    }

    @Override
    public BinaryOperator<Map<K, V>> combiner() {
        return (first, second) -> {
            first.putAll(second);
            return first;
        };
    }

    @Override
    public Function<Map<K, V>, ImmutableMap<K, V>> finisher() {
        return ImmutableMap::new;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
