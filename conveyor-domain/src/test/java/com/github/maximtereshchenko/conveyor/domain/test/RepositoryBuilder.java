package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.wiremock.client.WireMock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.maximtereshchenko.conveyor.wiremock.client.WireMock.get;
import static com.github.maximtereshchenko.conveyor.wiremock.client.WireMock.ok;

final class RepositoryBuilder {

    private final Map<Path, Consumer<OutputStream>> files = new HashMap<>();

    RepositoryBuilder schematicDefinition(SchematicDefinitionBuilder schematicDefinitionBuilder) {
        files.put(
            path(
                schematicDefinitionBuilder.group(),
                schematicDefinitionBuilder.name(),
                schematicDefinitionBuilder.version(),
                "json"
            ),
            schematicDefinitionBuilder::install
        );
        return this;
    }

    RepositoryBuilder jar(JarBuilder jarBuilder) {
        files.put(
            path(jarBuilder.group(), jarBuilder.name(), jarBuilder.version(), "jar"),
            jarBuilder::write
        );
        return this;
    }

    RepositoryBuilder remoteJar(JarBuilder jarBuilder) {
        files.put(
            remotePath(jarBuilder.group(), jarBuilder.name(), jarBuilder.version(), "jar"),
            jarBuilder::write
        );
        return this;
    }

    RepositoryBuilder pom(PomBuilder pomBuilder) {
        files.put(
            remotePath(
                pomBuilder.groupId(),
                pomBuilder.artifactId(),
                pomBuilder.version(),
                "pom"
            ),
            pomBuilder::write
        );
        return this;
    }

    void install(Path directory) {
        for (var entry : files.entrySet()) {
            try (var outputStream = outputStream(directory, entry.getKey())) {
                entry.getValue().accept(outputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    void install(WireMock wireMock) {
        for (var entry : files.entrySet()) {
            var outputStream = new ByteArrayOutputStream();
            entry.getValue().accept(outputStream);
            wireMock.register(
                get(entry.getKey().toString())
                    .willReturn(
                        ok()
                            .withHeader("Content-Type", contentType(entry.getKey()))
                            .withBody(outputStream.toByteArray())
                    )
            );
        }
    }

    private String contentType(Path path) {
        if (path.toString().endsWith("pom")) {
            return "text/xml";
        }
        return "application/octet-stream";
    }

    private OutputStream outputStream(Path directory, Path path) throws IOException {
        return Files.newOutputStream(Files.createDirectories(directory).resolve(path));
    }

    private Path path(String group, String name, String version, String extension) {
        return Paths.get("%s:%s-%s.%s".formatted(group, name, version, extension));
    }

    private Path remotePath(String group, String name, String version, String extension) {
        return Paths.get(
            "/%s/%s/%s/%s-%s.%s".formatted(
                group.replace('.', '/'),
                name,
                version,
                name,
                version,
                extension
            )
        );
    }
}
