package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;
import com.github.maximtereshchenko.conveyor.api.port.NoExplicitlyDefinedTemplate;
import com.github.maximtereshchenko.conveyor.api.port.PreferencesDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RemoteRepository implements Repository {

    private final URL url;
    private final XmlFactory xmlFactory;
    private final Http http;
    private final LocalDirectoryRepository cache;

    RemoteRepository(URL url, XmlFactory xmlFactory, Http http, LocalDirectoryRepository cache) {
        this.url = url;
        this.xmlFactory = xmlFactory;
        this.http = http;
        this.cache = cache;
    }

    @Override
    public Optional<ManualDefinition> manualDefinition(
        String name,
        SemanticVersion semanticVersion
    ) {
        return cache.manualDefinition(name, semanticVersion)
            .or(() ->
                absoluteUri(name, semanticVersion, "pom")
                    .flatMap(http::get)
                    .map(inputStream -> manualDefinition(name, semanticVersion, inputStream))
            );
    }

    @Override
    public Optional<Path> path(String name, SemanticVersion semanticVersion) {
        return cache.path(name, semanticVersion)
            .or(() ->
                absoluteUri(name, semanticVersion, "jar")
                    .flatMap(http::get)
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

    private Optional<URI> absoluteUri(
        String name,
        SemanticVersion semanticVersion,
        String classifier
    ) {
        if (!name.contains(":")) {
            return Optional.empty();
        }
        var groupAndArtifact = name.split(":");
        try {
            return Optional.of(
                url.toURI()
                    .resolve(
                        uri(
                            List.of(groupAndArtifact[0].split("\\.")),
                            groupAndArtifact[1],
                            semanticVersion,
                            classifier
                        )
                    )
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private URI uri(
        List<String> groupId,
        String artifactId,
        SemanticVersion semanticVersion,
        String classifier
    ) {
        return URI.create(
            Stream.concat(
                    groupId.stream(),
                    Stream.of(
                        artifactId,
                        semanticVersion,
                        "%s-%s.%s".formatted(artifactId, semanticVersion, classifier)
                    )
                )
                .map(Objects::toString)
                .collect(Collectors.joining("/", "/", ""))
        );
    }
}
