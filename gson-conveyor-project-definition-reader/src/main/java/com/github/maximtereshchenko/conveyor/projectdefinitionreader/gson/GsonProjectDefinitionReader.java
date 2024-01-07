package com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson;

import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ProjectDefinitionReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GsonProjectDefinitionReader implements ProjectDefinitionReader {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Path.class, new PathTypeAdapter())
        .create();

    @Override
    public ProjectDefinition projectDefinition(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, ProjectDefinition.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
