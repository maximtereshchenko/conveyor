package com.github.maximtereshchenko.conveyor.gson;

import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ExternalDependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.LocalProjectDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.google.gson.*;

import java.lang.reflect.Type;

final class DependencyDefinitionAdapter
    implements JsonSerializer<DependencyDefinition>, JsonDeserializer<DependencyDefinition> {

    @Override
    public JsonElement serialize(DependencyDefinition src, Type typeOfSrc, JsonSerializationContext context) {
        return switch (src) {
            case ExternalDependencyDefinition external -> {
                var jsonObject = new JsonObject();
                jsonObject.add("name", new JsonPrimitive(external.name()));
                jsonObject.add("version", new JsonPrimitive(external.version()));
                jsonObject.add("scope", new JsonPrimitive(external.scope().toString()));
                yield jsonObject;
            }
            case LocalProjectDependencyDefinition project -> {
                var jsonObject = new JsonObject();
                jsonObject.add("name", new JsonPrimitive(project.name()));
                yield jsonObject;
            }
        };
    }

    @Override
    public DependencyDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        var jsonObject = json.getAsJsonObject();
        var name = jsonObject.get("name").getAsString();
        var versionElement = jsonObject.get("version");
        if (versionElement == null) {
            return new LocalProjectDependencyDefinition(name);
        }
        return new ExternalDependencyDefinition(
            name,
            versionElement.getAsInt(),
            DependencyScope.valueOf(jsonObject.get("scope").getAsString())
        );
    }
}
