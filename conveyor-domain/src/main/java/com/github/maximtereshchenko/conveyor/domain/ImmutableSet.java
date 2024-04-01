package com.github.maximtereshchenko.conveyor.domain;

import java.util.Set;
import java.util.stream.Stream;

final class ImmutableSet<T> implements ImmutableCollection<T> {

    private final ImmutableCollection<T> original;

    ImmutableSet(ImmutableCollection<T> original) {
        this.original = original;
    }

    ImmutableSet(Set<T> set) {
        this(new ImmutableList<>(set));
    }

    @SafeVarargs
    ImmutableSet(T... elements) {
        this(Set.of(elements));
    }

    @Override
    public Stream<T> stream() {
        return original.stream()
            .distinct();
    }

    @Override
    public ImmutableSet<T> with(T element) {
        return new ImmutableSet<>(original.with(element));
    }

    @Override
    public boolean contains(T element) {
        return original.contains(element);
    }
}
