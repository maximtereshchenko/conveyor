package com.github.maximtereshchenko.conveyor.files;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<T, R> {

    R apply(T value) throws IOException;
}
