package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
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
        String group,
        String name,
        SemanticVersion semanticVersion
    ) {
        return cache.manualDefinition(group, name, semanticVersion)
            .or(() ->
                http.get(
                    absoluteUri(group, name, semanticVersion, "pom"),
                    inputStream -> manualDefinition(group, name, semanticVersion, inputStream)
                )
            );
    }

    @Override
    public Optional<Path> path(
        String group,
        String name,
        SemanticVersion semanticVersion
    ) {
        return cache.path(group, name, semanticVersion)
            .or(() ->
                http.get(
                    absoluteUri(group, name, semanticVersion, "jar"),
                    inputStream -> path(group, name, semanticVersion, inputStream)
                )
            );
    }

    private Path path(
        String group,
        String name,
        SemanticVersion semanticVersion,
        InputStream inputStream
    ) {
        try {
            return cache.storedJar(group, name, semanticVersion, inputStream.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ManualDefinition manualDefinition(
        String group,
        String name,
        SemanticVersion semanticVersion,
        InputStream inputStream
    ) {
        var xml = xmlFactory.xml(inputStream);
        return cache.stored(
            group,
            name,
            semanticVersion,
            new ManualDefinition(
                xml.text("groupId"),
                xml.text("artifactId"),
                version(xml),
                template(xml),
                Map.of(),
                new PreferencesDefinition(
                    List.of(),
                    artifacts(xml)
                ),
                List.of(),
                dependencies(xml)
            )
        );
    }

    private Collection<ArtifactPreferenceDefinition> artifacts(Xml xml) {
        return xml.tags("dependencyManagement")
            .stream()
            .map(dependencyManagement -> dependencyManagement.tags("dependencies"))
            .flatMap(Collection::stream)
            .map(dependencies -> dependencies.tags("dependency"))
            .flatMap(Collection::stream)
            .map(dependency ->
                new ArtifactPreferenceDefinition(
                    dependency.text("groupId"),
                    dependency.text("artifactId"),
                    version(xml)
                )
            )
            .toList();
    }

    private Collection<ManualDependencyDefinition> dependencies(Xml xml) {
        return xml.tags("dependencies")
            .stream()
            .map(dependencies -> dependencies.tags("dependency"))
            .flatMap(Collection::stream)
            .map(dependency ->
                new ManualDependencyDefinition(
                    dependency.text("groupId"),
                    dependency.text("artifactId"),
                    version(xml),
                    DependencyScope.IMPLEMENTATION
                )
            )
            .toList();
    }

    private TemplateForManualDefinition template(Xml xml) {
        return xml.tags("parent")
            .stream()
            .<TemplateForManualDefinition>map(parent ->
                new ManualTemplateDefinition(
                    parent.text("groupId"),
                    parent.text("artifactId"),
                    version(parent)
                )
            )
            .findAny()
            .orElseGet(NoTemplate::new);
    }

    private URI absoluteUri(
        String group,
        String name,
        SemanticVersion semanticVersion,
        String classifier
    ) {
        try {
            return url.toURI()
                .resolve(
                    uri(
                        List.of(group.split("\\.")),
                        name,
                        semanticVersion,
                        classifier
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

    private String version(Xml xml) {
        return xml.text("version");
    }
}
