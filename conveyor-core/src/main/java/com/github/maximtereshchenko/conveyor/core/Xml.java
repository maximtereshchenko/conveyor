package com.github.maximtereshchenko.conveyor.core;

import org.w3c.dom.Node;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Xml {

    private final Node node;

    Xml(Node node) {
        this.node = node;
    }

    String text(String tag) {
        return children(tag)
            .map(Node::getTextContent)
            .findAny()
            .orElseThrow();
    }

    Collection<Xml> tags(String name) {
        return children(name)
            .map(Xml::new)
            .toList();
    }

    private Stream<Node> children(String name) {
        return children()
            .filter(child -> child.getNodeName().equals(name));
    }

    private Stream<Node> children() {
        var childNodes = node.getChildNodes();
        return IntStream.range(0, childNodes.getLength())
            .mapToObj(childNodes::item);
    }
}
