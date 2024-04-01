package com.github.maximtereshchenko.conveyor.domain;

import org.w3c.dom.Document;

final class Xml {

    private final Document document;

    Xml(Document document) {
        this.document = document;
    }

    String text(String tag) {
        return document.getElementsByTagName(tag).item(0).getTextContent();
    }
}
