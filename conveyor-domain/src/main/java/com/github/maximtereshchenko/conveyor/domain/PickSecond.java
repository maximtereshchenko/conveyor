package com.github.maximtereshchenko.conveyor.domain;

import java.util.function.BinaryOperator;

final class PickSecond<T> implements BinaryOperator<T> {

    @Override
    public T apply(T first, T second) {
        return second;
    }
}
