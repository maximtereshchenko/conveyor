package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.schematic.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class SchematicDefinitionFactory {

    private final XmlFactory xmlFactory;

    SchematicDefinitionFactory(XmlFactory xmlFactory) {
        this.xmlFactory = xmlFactory;
    }

    SchematicDefinition schematicDefinition(InputStream inputStream) {
        return schematicDefinition(xmlFactory.xml(inputStream));
    }

    private SchematicDefinition schematicDefinition(Xml xml) {
        return new SchematicDefinition(
            groupId(xml),
            artifactId(xml),
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
        );
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
                    groupId(dependency),
                    artifactId(dependency),
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
                    groupId(dependency),
                    artifactId(dependency),
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
                    groupId(parent),
                    artifactId(parent),
                    version(parent)
                )
            )
            .findAny()
            .orElseGet(NoTemplateDefinition::new);
    }

    private String artifactId(Xml xml) {
        return xml.text("artifactId");
    }

    private String groupId(Xml xml) {
        return xml.text("groupId");
    }

    private String version(Xml xml) {
        return xml.text("version");
    }
}
