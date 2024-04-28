package com.github.maximtereshchenko.conveyor.core.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.*;

final class WireMockExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final ExtensionContext.Namespace namespace =
        ExtensionContext.Namespace.create(WireMockExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        var wireMockServer = wireMockServer(context);
        wireMockServer.start();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        wireMockServer(context).stop();
    }

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) {
        return parameterContext.getParameter().getType() == WireMockServer.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        return wireMockServer(extensionContext);
    }

    private WireMockServer wireMockServer(ExtensionContext extensionContext) {
        return extensionContext.getStore(namespace)
            .getOrComputeIfAbsent(
                WireMockServer.class,
                key -> new WireMockServer(WireMockConfiguration.options().dynamicPort()),
                WireMockServer.class
            );
    }
}
