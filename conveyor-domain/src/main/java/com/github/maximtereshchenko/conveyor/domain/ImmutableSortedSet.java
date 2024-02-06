package com.github.maximtereshchenko.conveyor.domain;

import java.util.Comparator;
import java.util.stream.Stream;

final class ImmutableSortedSet<T extends Comparable<T>> implements ImmutableCollection<T> {

    private final ImmutableSet<T> set;

    private ImmutableSortedSet(ImmutableSet<T> set) {
        this.set = set;
    }

    ImmutableSortedSet() {
        this(new ImmutableSet<>());
    }

    @Override
    public Stream<T> stream() {
        return set.stream()
            .sorted(Comparator.naturalOrder());
    }

    @Override
    public ImmutableSortedSet<T> with(T element) {
        return new ImmutableSortedSet<>(set.with(element));
    }

    @Override
    public boolean contains(T element) {
        return set.contains(element);
    }
}
