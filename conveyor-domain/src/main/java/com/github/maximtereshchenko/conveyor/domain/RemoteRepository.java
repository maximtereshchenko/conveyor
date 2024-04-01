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

final class RemoteRepository extends UriRepository {

    private final URL url;
    private final Http http;
    private final XmlFactory xmlFactory;
    private final SchematicDefinitionTranslator schematicDefinitionTranslator;
    private final LocalDirectoryRepository cache;

    RemoteRepository(
        URL url,
        Http http,
        XmlFactory xmlFactory,
        SchematicDefinitionTranslator schematicDefinitionTranslator,
        LocalDirectoryRepository cache
    ) {
        this.url = url;
        this.http = http;
        this.xmlFactory = xmlFactory;
        this.schematicDefinitionTranslator = schematicDefinitionTranslator;
        this.cache = cache;
    }

    @Override
    String extension(Classifier classifier) {
        return switch (classifier) {
            case SCHEMATIC_DEFINITION -> "pom";
            case MODULE -> "jar";
        };
    }

    @Override
    Optional<Path> path(URI uri, Classifier classifier) {
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
                                outputStream -> schematicDefinitionTranslator.write(
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

    private List<ArtifactPreferenceDefinition> artifacts(Xml xml) {
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

    private List<DependencyDefinition> dependencies(Xml xml) {
        return xml.tags("dependencies")
            .stream()
            .map(dependencies -> dependencies.tags("dependency"))
            .flatMap(Collection::stream)
            .map(dependency ->
                new DependencyDefinition(
                    dependency.text("groupId"),
                    dependency.text("artifactId"),
                    Optional.of(version(xml)),
                    Optional.of(DependencyScope.IMPLEMENTATION)
                )
            )
            .toList();
    }

    private TemplateDefinition template(Xml xml) {
        return xml.tags("parent")
            .stream()
            .<TemplateDefinition>map(parent ->
                new SchematicTemplateDefinition(
                    parent.text("groupId"),
                    parent.text("artifactId"),
                    version(parent)
                )
            )
            .findAny()
            .orElseGet(NoTemplateDefinition::new);
    }

    private String version(Xml xml) {
        return xml.text("version");
    }
}
