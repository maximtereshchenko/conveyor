package com.github.maximtereshchenko.conveyor.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class ImmutableListCollector<T> implements Collector<T, List<T>, ImmutableList<T>> {

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (first, second) -> {
            first.addAll(second);
            return first;
        };
    }

    @Override
    public Function<List<T>, ImmutableList<T>> finisher() {
        return ImmutableList::new;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
