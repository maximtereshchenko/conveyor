package com.github.maximtereshchenko.conveyor.filevisitors;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface PathConsumer {

    void accept(Path path) throws IOException;
}
