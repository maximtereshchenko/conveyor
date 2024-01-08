package com.github.maximtereshchenko.conveyor.api.port;

import java.nio.file.Path;

public interface JsonReader {

    <T> T read(Path path, Class<T> type);
}
