package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "project")
record PomModel(
    @JacksonXmlProperty(localName = "xmlns:xsi", isAttribute = true)
    String nsXsi,
    @JacksonXmlProperty(isAttribute = true)
    String xmlns,
    @JacksonXmlProperty(localName = "xsi:schemaLocation", isAttribute = true)
    String xsiSchemaLocation,
    String modelVersion,
    String groupId,
    String artifactId,
    String version
) {

    PomModel(String groupId, String artifactId, String version) {
        this(
            "http://www.w3.org/2001/XMLSchema-instance",
            "http://maven.apache.org/POM/4.0.0",
            "http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd",
            "4.0.0",
            groupId,
            artifactId,
            version
        );
    }
}
