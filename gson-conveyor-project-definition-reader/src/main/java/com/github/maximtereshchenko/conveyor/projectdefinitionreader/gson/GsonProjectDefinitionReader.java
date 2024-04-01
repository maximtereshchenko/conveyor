package com.github.maximtereshchenko.conveyor.projectdefinitionreader.gson;

import com.github.maximtereshchenko.conveyor.api.port.JsonReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GsonProjectDefinitionReader implements JsonReader {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Path.class, new PathTypeAdapter())
        .create();

    @Override
    public <T> T read(Path path, Class<T> type) {
        try (var reader = Files.newBufferedReader(path)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
