package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.jackson.annotation.JsonInclude;
import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collection;

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
    @JacksonXmlProperty(localName = "dependency")
    @JacksonXmlElementWrapper(localName = "dependencies")
    Collection<Dependency> dependencies
) {

    PomModel(
        Parent parent,
        String groupId,
        String artifactId,
        String version,
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
            dependencies
        );
    }

    record Parent(String groupId, String artifactId, String version) {}

    record Dependency(String groupId, String artifactId, String version) {}
}
