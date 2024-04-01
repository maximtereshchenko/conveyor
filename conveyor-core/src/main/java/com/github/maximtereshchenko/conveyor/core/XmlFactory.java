package com.github.maximtereshchenko.conveyor.core;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

final class XmlFactory {

    private final DocumentBuilder documentBuilder;

    private XmlFactory(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    static XmlFactory newInstance() {
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
            return new XmlFactory(factory.newDocumentBuilder());
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    Xml xml(InputStream inputStream) {
        try {
            var root = documentBuilder.parse(inputStream).getDocumentElement();
            root.normalize();
            return new Xml(root);
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
