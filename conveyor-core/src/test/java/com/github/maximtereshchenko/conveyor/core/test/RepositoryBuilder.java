package com.github.maximtereshchenko.conveyor.core.test;

import com.github.maximtereshchenko.conveyor.common.test.Directories;
import com.github.tomakehurst.wiremock.WireMockServer;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;

final class RepositoryBuilder {

    private final Set<URI> uris = new HashSet<>();
    private final Path temporaryDirectory;

    RepositoryBuilder(Path temporaryDirectory) {
        this.temporaryDirectory = temporaryDirectory;
    }

    RepositoryBuilder schematicDefinition(SchematicDefinitionBuilder schematicDefinitionBuilder)
        throws IOException {
        var uri = uri(
            schematicDefinitionBuilder.group(),
            schematicDefinitionBuilder.name(),
            schematicDefinitionBuilder.version(),
            "json"
        );
        uris.add(uri);
        schematicDefinitionBuilder.write(path(uri));
        return this;
    }

    RepositoryBuilder jar(JarBuilder jarBuilder) throws IOException {
        var uri = uri(jarBuilder.group(), jarBuilder.name(), jarBuilder.version(), "jar");
        uris.add(uri);
        jarBuilder.write(path(uri));
        return this;
    }

    RepositoryBuilder pom(PomBuilder pomBuilder) throws IOException {
        var uri = uri(
            pomBuilder.groupId(),
            pomBuilder.artifactId(),
            pomBuilder.version(),
            "pom"
        );
        uris.add(uri);
        pomBuilder.write(path(uri));
        return this;
    }

    void install(Path directory) throws IOException {
        for (var uri : uris) {
            var source = path(uri);
            Files.copy(
                source,
                Directories.createDirectoriesForFile(
                    directory.resolve(temporaryDirectory.relativize(source))
                )
            );
        }
    }

    void install(WireMockServer wireMockServer) throws IOException {
        for (var uri : uris) {
            try (var inputStream = Files.newInputStream(path(uri))) {
                wireMockServer.addStubMapping(
                    get("/" + uri)
                        .willReturn(
                            ok()
                                .withHeader("Content-Type", contentType(uri))
                                .withBody(inputStream.readAllBytes())
                        )
                        .build()
                );
            }
        }
    }

    private String contentType(URI uri) {
        if (uri.toString().endsWith("pom")) {
            return "text/xml";
        }
        return "application/octet-stream";
    }

    private Path path(URI uri) throws IOException {
        return Directories.createDirectoriesForFile(
            Paths.get(URI.create(temporaryDirectory.toUri().toString() + '/' + uri))
        );
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
