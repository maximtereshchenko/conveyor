package com.github.maximtereshchenko.conveyor.domain;

import java.util.stream.Stream;

interface ImmutableCollection<T> {

    Stream<T> stream();

    ImmutableCollection<T> with(T element);

    boolean contains(T element);
}
