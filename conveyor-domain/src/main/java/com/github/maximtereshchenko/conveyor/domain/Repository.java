package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

import java.nio.file.Files;
import java.nio.file.Path;

final class Repository {

    private final Path path;
    private final DefinitionReader definitionReader;

    Repository(Path path, DefinitionReader definitionReader) {
        this.path = path;
        this.definitionReader = definitionReader;
    }

    ManualDefinition manualDefinition(String name, int version) {
        return definitionReader.manualDefinition(path.resolve(fullName(name, version) + ".json"));
    }

    Path path(String name, int version) {
        var jar = path.resolve(fullName(name, version) + ".jar");
        if (Files.exists(jar)) {
            return jar;
        }
        throw new IllegalArgumentException(jar.toString());
    }

    private String fullName(String name, int version) {
        return name + '-' + version;
    }
}
