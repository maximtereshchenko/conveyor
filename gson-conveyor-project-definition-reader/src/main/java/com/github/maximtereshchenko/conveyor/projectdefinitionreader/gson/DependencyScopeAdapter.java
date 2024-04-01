package com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;
import java.util.Locale;

final class DependencyScopeAdapter implements JsonDeserializer<DependencyScope> {

    @Override
    public DependencyScope deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
        return DependencyScope.valueOf(element.getAsString().toUpperCase(Locale.ROOT));
    }
}
