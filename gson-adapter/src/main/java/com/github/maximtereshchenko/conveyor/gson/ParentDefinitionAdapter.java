package com.github.maximtereshchenko.conveyor.gson;

import com.github.maximtereshchenko.conveyor.api.port.NoExplicitParent;
import com.github.maximtereshchenko.conveyor.api.port.ParentDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ParentProjectDefinition;
import com.google.gson.*;

import java.lang.reflect.Type;

final class ParentDefinitionAdapter implements JsonSerializer<ParentDefinition>, JsonDeserializer<ParentDefinition> {

    @Override
    public JsonElement serialize(ParentDefinition src, Type typeOfSrc, JsonSerializationContext context) {
        return switch (src) {
            case NoExplicitParent ignored -> JsonNull.INSTANCE;
            case ParentProjectDefinition definition -> {
                var jsonObject = new JsonObject();
                jsonObject.add("name", new JsonPrimitive(definition.name()));
                jsonObject.add("version", new JsonPrimitive(definition.version()));
                yield jsonObject;
            }
        };
    }

    @Override
    public ParentDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        var jsonObject = json.getAsJsonObject();
        return new ParentProjectDefinition(jsonObject.get("name").getAsString(), jsonObject.get("version").getAsInt());
    }
}
