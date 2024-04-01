package com.github.maximtereshchenko.conveyor.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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
