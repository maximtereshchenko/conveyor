package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class RemoteRepository implements Repository {

    private final URL url;
    private final Http http;
    private final XmlFactory xmlFactory;
    private final DefinitionTranslator definitionTranslator;
    private final LocalDirectoryRepository cache;

    RemoteRepository(
        URL url,
        Http http,
        XmlFactory xmlFactory,
        DefinitionTranslator definitionTranslator,
        LocalDirectoryRepository cache
    ) {
        this.url = url;
        this.http = http;
        this.xmlFactory = xmlFactory;
        this.definitionTranslator = definitionTranslator;
        this.cache = cache;
    }

    @Override
    public Optional<Path> path(URI uri, Classifier classifier) {
        return cache.path(uri, classifier)
            .or(() -> {
                var absoluteUri = absoluteUri(uri);
                if (classifier == Classifier.SCHEMATIC_DEFINITION) {
                    http.get(
                        URI.create(absoluteUri.toString().replace(".json", ".pom")),
                        inputStream -> {
                            var xml = xmlFactory.xml(inputStream);
                            cache.stored(
                                uri,
                                outputStream -> definitionTranslator.write(
                                    new SchematicDefinition(
                                        xml.text("groupId"),
                                        xml.text("artifactId"),
                                        version(xml),
                                        template(xml),
                                        List.of(),
                                        List.of(),
                                        Map.of(),
                                        new PreferencesDefinition(
                                            List.of(),
                                            artifacts(xml)
                                        ),
                                        List.of(),
                                        dependencies(xml)
                                    ),
                                    outputStream
                                )
                            );
                        }
                    );
                } else {
                    http.get(
                        absoluteUri,
                        inputStream -> cache.stored(uri, inputStream::transferTo)
                    );
                }
                return cache.path(uri, classifier);
            });
    }

    private URI absoluteUri(URI uri) {
        try {
            return url.toURI().resolve(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
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

    private Collection<SchematicDependencyDefinition> dependencies(Xml xml) {
        return xml.tags("dependencies")
            .stream()
            .map(dependencies -> dependencies.tags("dependency"))
            .flatMap(Collection::stream)
            .<SchematicDependencyDefinition>map(dependency ->
                new DependencyOnArtifactDefinition(
                    dependency.text("groupId"),
                    dependency.text("artifactId"),
                    Optional.of(version(xml)),
                    Optional.of(DependencyScope.IMPLEMENTATION)
                )
            )
            .toList();
    }

    private TemplateForSchematicDefinition template(Xml xml) {
        return xml.tags("parent")
            .stream()
            .<TemplateForSchematicDefinition>map(parent ->
                new ManualTemplateDefinition(
                    parent.text("groupId"),
                    parent.text("artifactId"),
                    version(parent)
                )
            )
            .findAny()
            .orElseGet(NoTemplate::new);
    }

    private String version(Xml xml) {
        return xml.text("version");
    }
}
