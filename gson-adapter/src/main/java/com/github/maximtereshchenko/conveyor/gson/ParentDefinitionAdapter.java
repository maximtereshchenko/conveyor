package com.github.maximtereshchenko.conveyor.gson;

import com.github.maximtereshchenko.conveyor.api.port.ManualTemplateDefinition;
import com.github.maximtereshchenko.conveyor.api.port.NoExplicitTemplate;
import com.github.maximtereshchenko.conveyor.api.port.TemplateDefinition;
import com.google.gson.*;

import java.lang.reflect.Type;

final class ParentDefinitionAdapter implements JsonSerializer<TemplateDefinition>,
    JsonDeserializer<TemplateDefinition> {

    @Override
    public JsonElement serialize(TemplateDefinition src, Type typeOfSrc, JsonSerializationContext context) {
        return switch (src) {
            case NoExplicitTemplate ignored -> JsonNull.INSTANCE;
            case ManualTemplateDefinition definition -> {
                var jsonObject = new JsonObject();
                jsonObject.add("name", new JsonPrimitive(definition.name()));
                jsonObject.add("version", new JsonPrimitive(definition.version()));
                yield jsonObject;
            }
        };
    }

    @Override
    public TemplateDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        var jsonObject = json.getAsJsonObject();
        return new ManualTemplateDefinition(jsonObject.get("name").getAsString(), jsonObject.get("version").getAsInt());
    }
}
