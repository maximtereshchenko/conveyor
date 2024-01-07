package com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

final class PathTypeAdapter implements JsonDeserializer<Path> {

    @Override
    public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return Paths.get(json.getAsString());
    }
}
