package com.github.maximtereshchenko.conveyor.core.test;

import com.fasterxml.jackson.dataformat.xml.shadowed.XmlMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

final class PomBuilder {

    private final Collection<PomModel.Dependency> dependencies = new ArrayList<>();
    private final Collection<PomModel.Dependency> dependencyManagement = new ArrayList<>();
    private final String groupId = "com.github.maximtereshchenko.conveyor";
    private final Map<String, String> properties = new HashMap<>();
    private final String version = "1.0.0";
    private final XmlMapper xmlMapper;
    private PomModel.Parent parent = null;
    private String artifactId = "";

    PomBuilder(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    void write(Path path) throws IOException {
        try (var outputStream = Files.newOutputStream(path)) {
            outputStream.write(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                """.getBytes(StandardCharsets.UTF_8)
            );
            xmlMapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                    outputStream,
                    new PomModel(
                        parent,
                        groupId,
                        artifactId,
                        version,
                        properties,
                        new PomModel.DependencyManagement(dependencyManagement),
                        dependencies
                    )
                );
        }
    }

    String groupId() {
        return groupId;
    }

    String artifactId() {
        return artifactId;
    }

    String version() {
        return version;
    }

    PomBuilder artifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    PomBuilder parent(String artifactId) {
        parent = new PomModel.Parent(groupId, artifactId, version);
        return this;
    }

    PomBuilder dependency(String artifactId) {
        return dependency(artifactId, null);
    }

    PomBuilder dependency(String artifactId, String scope) {
        dependencies.add(new PomModel.Dependency(groupId, artifactId, version, scope));
        return this;
    }

    PomBuilder managedDependency(String artifactId, String version) {
        dependencyManagement.add(new PomModel.Dependency(groupId, artifactId, version, null));
        return this;
    }

    PomBuilder property(String key, String value) {
        properties.put(key, value);
        return this;
    }
}
