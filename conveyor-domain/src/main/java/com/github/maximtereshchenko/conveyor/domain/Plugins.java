package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.Products;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

final class Plugins {

    private final Set<Plugin> all;
    private final Properties properties;
    private final Dependencies dependencies;

    Plugins(Set<Plugin> all, Properties properties, Dependencies dependencies) {
        this.all = all;
        this.properties = properties;
        this.dependencies = dependencies;
    }

    Products executeTasks(Products products, Stage stage) {
        return conveyorPlugins()
            .filter(conveyorPlugin -> named(conveyorPlugin.name()).isEnabled())
            .flatMap(conveyorPlugin -> bindings(properties.conveyorProperties(), conveyorPlugin))
            .filter(binding -> isBeforeOrEqual(binding, stage))
            .sorted(byStageAndStep())
            .map(ConveyorTaskBinding::task)
            .reduce(
                products,
                (aggregated, task) -> task.execute(scopes -> dependencies.modulePath(Set.of(scopes)), aggregated),
                (a, b) -> a
            );
    }

    private Comparator<ConveyorTaskBinding> byStageAndStep() {
        return Comparator.comparing(ConveyorTaskBinding::stage).thenComparing(ConveyorTaskBinding::step);
    }

    private boolean isBeforeOrEqual(ConveyorTaskBinding conveyorTaskBinding, Stage stage) {
        return conveyorTaskBinding.stage().compareTo(stage) <= 0;
    }

    private Stream<ConveyorTaskBinding> bindings(ConveyorProperties conveyorProperties, ConveyorPlugin conveyorPlugin) {
        return conveyorPlugin.bindings(conveyorProperties, named(conveyorPlugin.name()).configuration())
            .stream();
    }

    private Stream<ConveyorPlugin> conveyorPlugins() {
        return ServiceLoader.load(moduleLayer(ModulePath.from(all).resolved()), ConveyorPlugin.class)
            .stream()
            .map(ServiceLoader.Provider::get);
    }

    private Plugin named(String name) {
        return all.stream()
            .filter(plugin -> plugin.name().equals(name))
            .findAny()
            .orElseThrow();
    }

    private ModuleLayer moduleLayer(Set<Path> paths) {
        var parent = getClass().getModule().getLayer();
        return parent.defineModulesWithOneLoader(
            parent.configuration()
                .resolveAndBind(
                    ModuleFinder.of(paths.toArray(Path[]::new)),
                    ModuleFinder.of(),
                    Set.of()
                ),
            Thread.currentThread().getContextClassLoader()
        );
    }
}
