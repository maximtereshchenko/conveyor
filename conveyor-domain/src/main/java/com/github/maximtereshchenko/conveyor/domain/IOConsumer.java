package com.github.maximtereshchenko.conveyor.domain;

import java.io.IOException;

interface IOConsumer<T> {

    void accept(T item) throws IOException;
}
