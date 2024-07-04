package com.github.maximtereshchenko.conveyor.files;

import java.io.IOException;

@FunctionalInterface
public interface IOConsumer<T> {

    void accept(T value) throws IOException;
}
