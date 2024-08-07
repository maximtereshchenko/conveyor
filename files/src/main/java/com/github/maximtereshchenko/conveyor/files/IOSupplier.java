package com.github.maximtereshchenko.conveyor.files;

import java.io.IOException;

@FunctionalInterface
public interface IOSupplier<T> {

    T get() throws IOException;
}
