package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.JacksonAdapter;
import com.github.maximtereshchenko.conveyor.jackson.dataformat.xml.XmlMapper;
import com.github.maximtereshchenko.conveyor.wiremock.junit5.WireMockRuntimeInfo;

final class BuilderFactory {

    private final JacksonAdapter gsonAdapter;
    private final XmlMapper xmlMapper;

    BuilderFactory(JacksonAdapter gsonAdapter, XmlMapper xmlMapper) {
        this.gsonAdapter = gsonAdapter;
        this.xmlMapper = xmlMapper;
    }

    SchematicBuilder schematicBuilder() {
        return new SchematicBuilder(gsonAdapter).name("project"); //TODO
    }

    RepositoryBuilder repositoryBuilder() {
        return new RepositoryBuilder(gsonAdapter);
    }

    RemoteRepositoryBuilder remoteRepositoryBuilder(WireMockRuntimeInfo wireMockRuntimeInfo) {
        return new RemoteRepositoryBuilder(wireMockRuntimeInfo, xmlMapper);
    }
}
