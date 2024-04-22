package com.github.maximtereshchenko.conveyor.core;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class PomDefinitionFactory {

    private final DocumentBuilder documentBuilder;

    private PomDefinitionFactory(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    static PomDefinitionFactory configured() {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        try {
            factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
            );
            factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
            );
            factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
            );
            factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
            );
            return new PomDefinitionFactory(factory.newDocumentBuilder());
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    PomDefinition pomDefinition(InputStream inputStream) {
        try {
            var root = documentBuilder.parse(inputStream).getDocumentElement();
            root.normalize();
            return new PomDefinition(
                parent(root),
                groupId(root),
                artifactId(root),
                version(root),
                properties(root),
                dependencyManagement(root),
                dependencies(root, this::dependencyDefinition)
            );
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<PomDefinition.Parent> parent(Node root) {
        return namedChildren(root, "parent")
            .map(parent ->
                new PomDefinition.Parent(
                    groupId(parent).orElseThrow(),
                    artifactId(parent),
                    version(parent).orElseThrow()
                )
            )
            .findAny();
    }

    private List<PomDefinition.ManagedDependencyDefinition> dependencyManagement(Element root) {
        return namedChildren(root, "dependencyManagement")
            .map(dependencyManagement ->
                dependencies(dependencyManagement, this::managedDependencyDefinition)
            )
            .flatMap(Collection::stream)
            .toList();
    }

    private Map<String, String> properties(Node root) {
        return namedChildren(root, "properties")
            .flatMap(node -> children(node, child -> child.getNodeType() == Node.ELEMENT_NODE))
            .collect(Collectors.toMap(Node::getNodeName, Node::getTextContent));
    }

    private <T> List<T> dependencies(Node node, Function<Node, T> mapper) {
        return namedChildren(node, "dependencies")
            .flatMap(dependencies -> namedChildren(dependencies, "dependency"))
            .map(mapper)
            .toList();
    }

    private PomDefinition.DependencyDefinition dependencyDefinition(Node node) {
        return new PomDefinition.DependencyDefinition(
            groupId(node).orElseThrow(),
            artifactId(node),
            version(node),
            singleValue(node, "scope")
                .map(String::toUpperCase)
                .map(PomDefinition.DependencyScope::valueOf)
        );
    }

    private PomDefinition.ManagedDependencyDefinition managedDependencyDefinition(Node node) {
        return new PomDefinition.ManagedDependencyDefinition(
            groupId(node).orElseThrow(),
            artifactId(node),
            version(node).orElseThrow(),
            singleValue(node, "scope")
                .map(String::toUpperCase)
                .map(PomDefinition.ManagedDependencyScope::valueOf)
        );
    }

    private Optional<String> version(Node node) {
        return singleValue(node, "version");
    }

    private Optional<String> groupId(Node node) {
        return singleValue(node, "groupId");
    }

    private String artifactId(Node node) {
        return singleValue(node, "artifactId").orElseThrow();
    }

    private Optional<String> singleValue(Node node, String tag) {
        return namedChildren(node, tag)
            .map(Node::getTextContent)
            .filter(content -> !content.isBlank())
            .findAny();
    }

    private Stream<Node> namedChildren(Node node, String name) {
        return children(node, child -> child.getNodeName().equals(name));
    }

    private Stream<Node> children(Node node, Predicate<Node> predicate) {
        var childNodes = node.getChildNodes();
        return IntStream.range(0, childNodes.getLength())
            .mapToObj(childNodes::item)
            .filter(predicate);
    }
}
