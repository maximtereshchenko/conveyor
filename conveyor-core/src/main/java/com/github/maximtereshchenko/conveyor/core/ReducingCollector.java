package com.github.maximtereshchenko.conveyor.core;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class ReducingCollector<T, C> implements Collector<T, LinkedHashMap<C, T>, LinkedHashSet<T>> {

    private final Function<T, C> classifier;
    private final BinaryOperator<T> combiner;

    ReducingCollector(Function<T, C> classifier, BinaryOperator<T> combiner) {
        this.classifier = classifier;
        this.combiner = combiner;
    }

    ReducingCollector(Function<T, C> classifier) {
        this(classifier, (next, existing) -> next);
    }

    @Override
    public Supplier<LinkedHashMap<C, T>> supplier() {
        return LinkedHashMap::new;
    }

    @Override
    public BiConsumer<LinkedHashMap<C, T>, T> accumulator() {
        return (map, element) -> map.compute(
            classifier.apply(element),
            (key, existing) -> existing == null ? element : combiner.apply(element, existing)
        );
    }

    @Override
    public BinaryOperator<LinkedHashMap<C, T>> combiner() {
        return (first, second) -> {
            first.putAll(second);
            return first;
        };
    }

    @Override
    public Function<LinkedHashMap<C, T>, LinkedHashSet<T>> finisher() {
        return map -> new LinkedHashSet<>(map.values());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
