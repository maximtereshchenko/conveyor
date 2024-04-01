package com.github.maximtereshchenko.conveyor.gson;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDependencyDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.google.gson.*;

import java.lang.reflect.Type;

final class DependencyDefinitionAdapter
    implements JsonSerializer<DependencyDefinition>, JsonDeserializer<DependencyDefinition> {

    @Override
    public JsonElement serialize(DependencyDefinition src, Type typeOfSrc, JsonSerializationContext context) {
        return switch (src) {
            case ArtifactDependencyDefinition artifact -> {
                var jsonObject = new JsonObject();
                jsonObject.add("name", new JsonPrimitive(artifact.name()));
                jsonObject.add("version", new JsonPrimitive(artifact.version()));
                jsonObject.add("scope", new JsonPrimitive(artifact.scope().toString()));
                yield jsonObject;
            }
            case SchematicDependencyDefinition schematic -> {
                var jsonObject = new JsonObject();
                jsonObject.add("name", new JsonPrimitive(schematic.schematic()));
                jsonObject.add("scope", new JsonPrimitive(schematic.scope().toString()));
                yield jsonObject;
            }
        };
    }

    @Override
    public DependencyDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        var jsonObject = json.getAsJsonObject();
        var name = jsonObject.get("name").getAsString();
        var scope = DependencyScope.valueOf(jsonObject.get("scope").getAsString());
        var versionElement = jsonObject.get("version");
        if (versionElement == null) {
            return new SchematicDependencyDefinition(name, scope);
        }
        return new ArtifactDependencyDefinition(name, versionElement.getAsInt(), scope);
    }
}
