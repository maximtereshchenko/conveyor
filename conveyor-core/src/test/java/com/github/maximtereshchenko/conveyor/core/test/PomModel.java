package com.github.maximtereshchenko.conveyor.core.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.*;

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
    Properties properties,
    DependencyManagement dependencyManagement,
    @JacksonXmlProperty(localName = "dependency")
    @JacksonXmlElementWrapper(localName = "dependencies")
    List<Dependency> dependencies
) {

    PomModel(
        Parent parent,
        String groupId,
        String artifactId,
        String version,
        Properties properties,
        DependencyManagement dependencyManagement,
        List<Dependency> dependencies
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

    public static class Properties implements Map<String, String> {

        private final List<Map.Entry<String, String>> entries = new ArrayList<>();

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            return entries.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String get(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String put(String key, String value) {
            entries.add(Map.entry(key, value));
            return "";
        }

        @Override
        public String remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> values() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return new LinkedHashSet<>(entries);
        }
    }
}
