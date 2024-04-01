package com.github.maximtereshchenko.conveyor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class OverridingCollector<T, C> implements Collector<T, Map<C, T>, Set<T>> {

    private final Function<T, C> classifier;
    private final BinaryOperator<T> combiner;

    OverridingCollector(Function<T, C> classifier, BinaryOperator<T> combiner) {
        this.classifier = classifier;
        this.combiner = combiner;
    }

    @Override
    public Supplier<Map<C, T>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<C, T>, T> accumulator() {
        return (map, element) -> map.compute(
            classifier.apply(element),
            (key, existing) -> existing == null ? element : combiner.apply(element, existing)
        );
    }

    @Override
    public BinaryOperator<Map<C, T>> combiner() {
        return (a, b) -> a;
    }

    @Override
    public Function<Map<C, T>, Set<T>> finisher() {
        return map -> Set.copyOf(map.values());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
