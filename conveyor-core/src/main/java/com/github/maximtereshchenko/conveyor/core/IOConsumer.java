package com.github.maximtereshchenko.conveyor.core;

import java.io.IOException;

interface IOConsumer<T> {

    void accept(T item) throws IOException;
}
