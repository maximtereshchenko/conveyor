module com.github.maximtereshchenko.conveyor.wiremock {
    requires org.junit.jupiter.api;
    requires org.slf4j;
    requires java.xml;
    requires java.logging;
    requires com.fasterxml.jackson.core;
    requires org.apache.commons.lang3;
    requires com.google.common;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.http2.server;
    requires org.eclipse.jetty.servlet;
    requires org.eclipse.jetty.servlets;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    opens com.github.maximtereshchenko.conveyor.wiremock.junit5 to org.junit.platform.commons;
    exports com.github.maximtereshchenko.conveyor.wiremock.junit5;
    exports com.github.maximtereshchenko.conveyor.wiremock.client;
    exports com.github.maximtereshchenko.conveyor.wiremock.servlet;
    exports com.github.maximtereshchenko.conveyor.wiremock.http;
    exports com.github.maximtereshchenko.conveyor.wiremock.verification;
    exports com.github.maximtereshchenko.conveyor.wiremock.verification.diff;
}