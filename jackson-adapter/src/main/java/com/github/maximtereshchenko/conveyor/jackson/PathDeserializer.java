package com.github.maximtereshchenko.conveyor.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

final class PathDeserializer extends StdDeserializer<Path> {

    private final FileSystem fileSystem;

    PathDeserializer(FileSystem fileSystem) {
        super(Path.class);
        this.fileSystem = fileSystem;
    }

    @Override
    public Path deserialize(JsonParser jsonParser, DeserializationContext context)
        throws IOException {
        return fileSystem.getPath(jsonParser.getValueAsString());
    }
}
