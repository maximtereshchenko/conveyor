package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;

final class ModuleLoader {

    Collection<ConveyorPlugin> conveyorPlugins(Collection<Path> artifacts) {
        return ServiceLoader.load(moduleLayer(artifacts), ConveyorPlugin.class)
            .stream()
            .map(Provider::get)
            .toList();
    }

    private ModuleLayer moduleLayer(Collection<Path> artifacts) {
        var parent = getClass().getModule().getLayer();
        var configuration = parent.configuration()
            .resolveAndBind(
                ModuleFinder.of(artifacts.toArray(Path[]::new)),
                ModuleFinder.of(),
                Set.of()
            );
        return parent.defineModulesWithOneLoader(configuration, Thread.currentThread().getContextClassLoader());
    }
}
