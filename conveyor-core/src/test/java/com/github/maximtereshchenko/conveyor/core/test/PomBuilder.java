package com.github.maximtereshchenko.conveyor.core.test;

import com.fasterxml.jackson.dataformat.xml.shadowed.XmlMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

final class PomBuilder {

    private final Collection<PomModel.Dependency> dependencies = new ArrayList<>();
    private final Collection<PomModel.Dependency> dependencyManagement = new ArrayList<>();
    private final Map<String, String> properties = new HashMap<>();
    private final XmlMapper xmlMapper;
    private String groupId = "group";
    private String artifactId = "";
    private String version = "1.0.0";
    private PomModel.Parent parent = null;

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
        if (groupId == null) {
            return parent.groupId();
        }
        return groupId;
    }

    String artifactId() {
        return artifactId;
    }

    String version() {
        if (version == null) {
            return parent.version();
        }
        return version;
    }

    PomBuilder groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    PomBuilder version(String version) {
        this.version = version;
        return this;
    }

    PomBuilder artifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    PomBuilder parent(String artifactId) {
        return parent(groupId, artifactId, version);
    }

    PomBuilder parent(String groupId, String artifactId, String version) {
        parent = new PomModel.Parent(groupId, artifactId, version);
        return this;
    }

    PomBuilder dependency(String artifactId) {
        return dependency(groupId, artifactId, version, null);
    }

    PomBuilder dependency(
        String groupId,
        String artifactId,
        String version,
        String scope,
        PomModel.Exclusion... exclusions
    ) {
        dependencies.add(
            new PomModel.Dependency(
                groupId,
                artifactId,
                version,
                scope,
                List.of(exclusions)
            )
        );
        return this;
    }

    PomBuilder managedDependency(String artifactId, String version) {
        return managedDependency(artifactId, version, null);
    }

    PomBuilder managedDependency(String artifactId, String version, String scope) {
        dependencyManagement.add(
            new PomModel.Dependency(groupId, artifactId, version, scope, null)
        );
        return this;
    }

    PomBuilder property(String key, String value) {
        properties.put(key, value);
        return this;
    }
}
