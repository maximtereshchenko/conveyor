package com.github.maximtereshchenko.conveyor.gson;

import com.github.maximtereshchenko.conveyor.api.port.ParentDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

final class ParentDefinitionAdapter implements JsonSerializer<ParentDefinition> {

    @Override
    public JsonElement serialize(ParentDefinition src, Type typeOfSrc, JsonSerializationContext context) {
        return JsonNull.INSTANCE;
    }
}
