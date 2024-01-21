package com.github.maximtereshchenko.conveyor.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

final class PathTypeAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {

    @Override
    public JsonElement serialize(Path path, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(path.toString());
    }

    @Override
    public Path deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
        return Paths.get(element.getAsString());
    }
}
