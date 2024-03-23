package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.XmlMapper;
import com.github.maximtereshchenko.conveyor.wiremock.junit5.WireMockRuntimeInfo;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.UnaryOperator;

final class RemoteRepositoryBuilder {

    private final WireMockRuntimeInfo wireMockRuntimeInfo;
    private final XmlMapper xmlMapper;

    RemoteRepositoryBuilder(WireMockRuntimeInfo wireMockRuntimeInfo, XmlMapper xmlMapper) {
        this.wireMockRuntimeInfo = wireMockRuntimeInfo;
        this.xmlMapper = xmlMapper;
    }

    RemoteRepositoryBuilder artifact(
        UnaryOperator<RemoteArtifactBuilder> remoteArtifactBuilderConfiguration
    ) {
        remoteArtifactBuilderConfiguration.apply(new RemoteArtifactBuilder(xmlMapper))
            .install(wireMockRuntimeInfo.getWireMock());
        return this;
    }

    URL url() {
        try {
            return URI.create(wireMockRuntimeInfo.getHttpBaseUrl()).toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
