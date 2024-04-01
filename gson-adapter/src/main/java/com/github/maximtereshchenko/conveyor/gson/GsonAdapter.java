package com.github.maximtereshchenko.conveyor.gson;

import com.github.maximtereshchenko.conveyor.api.port.ParentDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GsonAdapter implements ProjectDefinitionReader {

    private final Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
        .registerTypeHierarchyAdapter(ParentDefinition.class, new ParentDefinitionAdapter())
        .registerTypeAdapter(DependencyScope.class, new DependencyScopeAdapter())
        .create();

    @Override
    public ProjectDefinition projectDefinition(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, ProjectDefinition.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void write(Path path, Object object) {
        try (var writer = Files.newBufferedWriter(path)) {
            gson.toJson(object, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
