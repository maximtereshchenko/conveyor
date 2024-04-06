package com.github.maximtereshchenko.conveyor.core;

import org.w3c.dom.Node;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

final class Xml {

    private final Node node;

    Xml(Node node) {
        this.node = node;
    }

    String name() {
        return node.getNodeName();
    }

    String text() {
        return node.getTextContent();
    }

    List<Xml> tags(String name) {
        return children(child -> child.getNodeName().equals(name));
    }

    List<Xml> tags() {
        return children(child -> child.getNodeType() == Node.ELEMENT_NODE);
    }

    private List<Xml> children(Predicate<Node> predicate) {
        var childNodes = node.getChildNodes();
        return IntStream.range(0, childNodes.getLength())
            .mapToObj(childNodes::item)
            .filter(predicate)
            .map(Xml::new)
            .toList();
    }
}
