package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;
import com.github.maximtereshchenko.conveyor.api.port.NoExplicitlyDefinedTemplate;
import com.github.maximtereshchenko.conveyor.api.port.PreferencesDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RemoteRepository implements Repository {

    private final URL url;
    private final XmlFactory xmlFactory;
    private final HttpClient httpClient;
    private final LocalDirectoryRepository cache;

    RemoteRepository(URL url, XmlFactory xmlFactory, LocalDirectoryRepository cache) {
        this.url = url;
        this.xmlFactory = xmlFactory;
        this.httpClient = HttpClient.newHttpClient();
        this.cache = cache;
    }

    @Override
    public Optional<ManualDefinition> manualDefinition(
        String name,
        SemanticVersion semanticVersion
    ) {
        return cache.manualDefinition(name, semanticVersion)
            .or(() ->
                uri(name, semanticVersion, "pom")
                    .flatMap(this::inputStream)
                    .map(inputStream -> manualDefinition(name, semanticVersion, inputStream))
            );
    }

    @Override
    public Optional<Path> path(String name, SemanticVersion semanticVersion) {
        return cache.path(name, semanticVersion)
            .or(() ->
                uri(name, semanticVersion, "jar")
                    .flatMap(this::inputStream)
                    .map(inputStream -> path(name, semanticVersion, inputStream))
            );
    }

    private Path path(String name, SemanticVersion semanticVersion, InputStream inputStream) {
        try (inputStream) {
            return cache.storedJar(name, semanticVersion, inputStream.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ManualDefinition manualDefinition(
        String name,
        SemanticVersion semanticVersion,
        InputStream inputStream
    ) {
        try (inputStream) {
            var xml = xmlFactory.xml(inputStream);
            return cache.stored(
                name,
                semanticVersion,
                new ManualDefinition(
                    xml.text("groupId") + ':' + xml.text("artifactId"),
                    xml.text("version"),
                    new NoExplicitlyDefinedTemplate(),
                    Map.of(),
                    new PreferencesDefinition(),
                    List.of(),
                    List.of()
                )
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Optional<String> uri(String name, SemanticVersion semanticVersion, String classifier) {
        if (!name.contains(":")) {
            return Optional.empty();
        }
        var groupAndArtifact = name.split(":");
        return Optional.of(
            Stream.concat(
                    Stream.of(groupAndArtifact[0].split("\\.")),
                    Stream.of(
                        groupAndArtifact[1],
                        semanticVersion,
                        "%s-%s.%s".formatted(groupAndArtifact[1], semanticVersion, classifier)
                    )
                )
                .map(Object::toString)
                .collect(Collectors.joining("/", "/", ""))
        );
    }

    private Optional<InputStream> inputStream(String uri) {
        var response = response(uri);
        if (response.statusCode() != 200) {
            return Optional.empty();
        }
        return Optional.of(response.body());
    }

    private HttpResponse<InputStream> response(String uri) {
        try {
            return httpClient.send(
                HttpRequest.newBuilder()
                    .GET()
                    .uri(url.toURI().resolve(uri))
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
