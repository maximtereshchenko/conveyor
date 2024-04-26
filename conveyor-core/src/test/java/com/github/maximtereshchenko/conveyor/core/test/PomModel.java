package com.github.maximtereshchenko.conveyor.core.test;

import com.fasterxml.jackson.annotation.shadowed.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.shadowed.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.shadowed.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.shadowed.annotation.JacksonXmlRootElement;

import java.util.Collection;
import java.util.Map;

@JacksonXmlRootElement(localName = "project")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
record PomModel(
    @JacksonXmlProperty(localName = "xmlns:xsi", isAttribute = true)
    String nsXsi,
    @JacksonXmlProperty(isAttribute = true)
    String xmlns,
    @JacksonXmlProperty(localName = "xsi:schemaLocation", isAttribute = true)
    String xsiSchemaLocation,
    Parent parent,
    String modelVersion,
    String groupId,
    String artifactId,
    String version,
    Map<String, String> properties,
    DependencyManagement dependencyManagement,
    @JacksonXmlProperty(localName = "dependency")
    @JacksonXmlElementWrapper(localName = "dependencies")
    Collection<Dependency> dependencies
) {

    PomModel(
        Parent parent,
        String groupId,
        String artifactId,
        String version,
        Map<String, String> properties,
        DependencyManagement dependencyManagement,
        Collection<Dependency> dependencies
    ) {
        this(
            "http://www.w3.org/2001/XMLSchema-instance",
            "http://maven.apache.org/POM/4.0.0",
            "http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd",
            parent,
            "4.0.0",
            groupId,
            artifactId,
            version,
            properties,
            dependencyManagement,
            dependencies
        );
    }

    record Parent(String groupId, String artifactId, String version) {}

    record Dependency(
        String groupId,
        String artifactId,
        String version,
        String scope,
        @JacksonXmlProperty(localName = "exclusion")
        @JacksonXmlElementWrapper(localName = "exclusions")
        Collection<Exclusion> exclusions
    ) {}

    record DependencyManagement(
        @JacksonXmlProperty(localName = "dependency")
        @JacksonXmlElementWrapper(localName = "dependencies")
        Collection<Dependency> dependencies
    ) {}

    record Exclusion(String groupId, String artifactId) {}
}
