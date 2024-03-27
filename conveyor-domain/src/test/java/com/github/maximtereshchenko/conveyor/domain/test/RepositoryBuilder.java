package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.wiremock.client.WireMock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.maximtereshchenko.conveyor.wiremock.client.WireMock.get;
import static com.github.maximtereshchenko.conveyor.wiremock.client.WireMock.ok;

final class RepositoryBuilder {

    private final Map<URI, Consumer<OutputStream>> files = new HashMap<>();

    RepositoryBuilder schematicDefinition(SchematicDefinitionBuilder schematicDefinitionBuilder) {
        files.put(
            uri(
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
            uri(jarBuilder.group(), jarBuilder.name(), jarBuilder.version(), "jar"),
            jarBuilder::write
        );
        return this;
    }

    RepositoryBuilder pom(PomBuilder pomBuilder) {
        files.put(
            uri(
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
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    void install(WireMock wireMock) {
        for (var entry : files.entrySet()) {
            var outputStream = new ByteArrayOutputStream();
            entry.getValue().accept(outputStream);
            wireMock.register(
                get("/" + entry.getKey().toString())
                    .willReturn(
                        ok()
                            .withHeader("Content-Type", contentType(entry.getKey()))
                            .withBody(outputStream.toByteArray())
                    )
            );
        }
    }

    private String contentType(URI uri) {
        if (uri.toString().endsWith("pom")) {
            return "text/xml";
        }
        return "application/octet-stream";
    }

    private OutputStream outputStream(Path directory, URI uri)
        throws IOException, URISyntaxException {
        var path = Paths.get(URI.create(directory.toUri().toString() + '/' + uri));
        Files.createDirectories(path.getParent());
        return Files.newOutputStream(path);
    }

    private URI uri(String group, String name, String version, String extension) {
        return URI.create(
            "%s/%s/%s/%s-%s.%s".formatted(
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
