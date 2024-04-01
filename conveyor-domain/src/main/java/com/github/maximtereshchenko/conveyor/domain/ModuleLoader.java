package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;

import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.stream.Collectors;

final class ModuleLoader {

    Set<ConveyorPlugin> conveyorPlugins(Set<Path> artifacts) {
        return ServiceLoader.load(moduleLayer(artifacts), ConveyorPlugin.class)
            .stream()
            .map(Provider::get)
            .collect(Collectors.toSet());
    }

    private ModuleLayer moduleLayer(Set<Path> artifacts) {
        var parent = getClass().getModule().getLayer();
        return parent.defineModulesWithOneLoader(
            parent.configuration()
                .resolveAndBind(
                    ModuleFinder.of(artifacts.toArray(Path[]::new)),
                    ModuleFinder.of(),
                    Set.of()
                ),
            Thread.currentThread().getContextClassLoader()
        );
    }
}
