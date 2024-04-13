package com.github.maximtereshchenko.zip;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
interface OutputStreamConsumer {

    void consume(OutputStream outputStream) throws IOException;
}
