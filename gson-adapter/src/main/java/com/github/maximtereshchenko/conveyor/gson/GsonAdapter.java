package com.github.maximtereshchenko.conveyor.gson;

import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import com.github.maximtereshchenko.conveyor.api.port.StoredArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.StoredArtifactDefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GsonAdapter implements ProjectDefinitionReader, StoredArtifactDefinitionReader {

    private final Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
        .registerTypeAdapter(DependencyScope.class, new DependencyScopeAdapter())
        .create();

    @Override
    public ProjectDefinition projectDefinition(Path path) {
        return read(path, ProjectDefinition.class);
    }

    @Override
    public StoredArtifactDefinition storedArtifactDefinition(Path path) {
        return read(path, StoredArtifactDefinition.class);
    }

    public void write(Path path, Object object) {
        try (var writer = Files.newBufferedWriter(path)) {
            gson.toJson(object, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> T read(Path path, Class<T> type) {
        try (var reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
