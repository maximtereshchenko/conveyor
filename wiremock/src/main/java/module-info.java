module com.github.tomakehurst.wiremock.shadowed {
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
    exports com.github.tomakehurst.wiremock.shadowed;
    exports com.github.tomakehurst.wiremock.shadowed.core;
    exports com.github.tomakehurst.wiremock.shadowed.client;
    exports com.github.tomakehurst.wiremock.shadowed.servlet;
    exports com.github.tomakehurst.wiremock.shadowed.http;
    exports com.github.tomakehurst.wiremock.shadowed.verification;
    exports com.github.tomakehurst.wiremock.shadowed.verification.diff;
    exports com.github.tomakehurst.wiremock.shadowed.stubbing;
    exports com.github.tomakehurst.wiremock.shadowed.admin.model;
}