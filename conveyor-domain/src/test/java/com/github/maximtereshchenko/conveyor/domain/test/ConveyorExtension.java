package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.domain.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.function.Supplier;

final class ConveyorExtension implements ParameterResolver {

    private final Namespace namespace = Namespace.create(ConveyorExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var type = parameterContext.getParameter().getType();
        return type == ConveyorModule.class || type == ArtifactFactory.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var gsonAdapter = get(extensionContext, GsonAdapter.class, GsonAdapter::new);
        if (parameterContext.getParameter().getType() == ConveyorModule.class) {
            return get(extensionContext, ConveyorModule.class, () -> new ConveyorFacade(gsonAdapter));
        }
        return get(extensionContext, ArtifactFactory.class, () -> new ArtifactFactory(gsonAdapter));
    }

    private <T> T get(ExtensionContext context, Class<T> type, Supplier<T> creator) {
        return context.getStore(namespace).getOrComputeIfAbsent(type, key -> creator.get(), type);
    }
}
