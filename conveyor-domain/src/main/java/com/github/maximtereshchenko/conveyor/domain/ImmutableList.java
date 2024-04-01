package com.github.maximtereshchenko.conveyor.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

final class ImmutableList<T> implements ImmutableCollection<T> {

    private final List<T> list;

    ImmutableList(Collection<T> collection) {
        this.list = List.copyOf(collection);
    }

    @Override
    public Stream<T> stream() {
        return list.stream();
    }

    @Override
    public ImmutableList<T> with(T element) {
        var copy = new ArrayList<>(list);
        copy.add(element);
        return new ImmutableList<>(copy);
    }

    @Override
    public boolean contains(T element) {
        return list.contains(element);
    }
}
