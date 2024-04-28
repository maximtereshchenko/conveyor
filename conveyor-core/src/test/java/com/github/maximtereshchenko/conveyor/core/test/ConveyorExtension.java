package com.github.maximtereshchenko.conveyor.core.test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.maximtereshchenko.compiler.Compiler;
import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.core.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

final class ConveyorExtension implements ParameterResolver {

    private final Namespace namespace = Namespace.create(ConveyorExtension.class);

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) {
        var type = parameterContext.getParameter().getType();
        return type == ConveyorModule.class || type == BuilderFactory.class;
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext
    ) {
        var store = extensionContext.getStore(namespace);
        var jacksonAdapter = store.getOrComputeIfAbsent(
            JacksonAdapter.class,
            key -> JacksonAdapter.configured(),
            JacksonAdapter.class
        );
        store.getOrComputeIfAbsent(
            ConveyorModule.class,
            key -> new ConveyorFacade(jacksonAdapter)
        );
        store.getOrComputeIfAbsent(
            BuilderFactory.class,
            key -> new BuilderFactory(jacksonAdapter, new XmlMapper(), new Compiler())
        );
        return store.get(parameterContext.getParameter().getType());
    }
}
