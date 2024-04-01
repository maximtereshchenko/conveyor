package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.Products;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

final class Plugins {

    private final ImmutableMap<String, Plugin> indexed;

    private Plugins(ImmutableMap<String, Plugin> indexed) {
        this.indexed = indexed;
    }

    Plugins() {
        this(new ImmutableMap<>());
    }

    static Plugins from(ImmutableSet<Plugin> plugins) {
        return new Plugins(
            plugins.stream()
                .collect(new ImmutableMapCollector<>(Plugin::name))
        );
    }

    Products executeTasks(
        Products products,
        Repository repository,
        Properties properties,
        Dependencies dependencies,
        Stage stage
    ) {
        return conveyorPlugins(repository)
            .filter(conveyorPlugin -> isEnabled(conveyorPlugin.name()))
            .flatMap(conveyorPlugin -> bindings(properties, conveyorPlugin))
            .filter(binding -> isBeforeOrEqual(binding, stage))
            .sorted(byStageAndStep())
            .map(ConveyorTaskBinding::task)
            .reduce(
                products,
                (aggregated, task) -> task.execute(
                    scopes -> dependencies.modulePath(repository, new ImmutableSet<>(scopes)),
                    aggregated
                ),
                new PickSecond<>()
            );
    }

    Plugins override(Plugins base) {
        return new Plugins(
            Stream.concat(
                    base.indexed.values().stream(),
                    indexed.values().stream()
                )
                .collect(new ImmutableMapCollector<>(Plugin::name, (present, inserted) -> inserted.override(present)))
        );
    }

    private Comparator<ConveyorTaskBinding> byStageAndStep() {
        return Comparator.comparing(ConveyorTaskBinding::stage).thenComparing(ConveyorTaskBinding::step);
    }

    private boolean isBeforeOrEqual(ConveyorTaskBinding conveyorTaskBinding, Stage stage) {
        return conveyorTaskBinding.stage().compareTo(stage) <= 0;
    }

    private Stream<ConveyorTaskBinding> bindings(Properties properties, ConveyorPlugin conveyorPlugin) {
        return conveyorPlugin.bindings(
                properties.conveyorProperties(),
                interpolatedConfiguration(conveyorPlugin.name(), properties)
            )
            .stream();
    }

    private Stream<ConveyorPlugin> conveyorPlugins(Repository repository) {
        return ServiceLoader.load(moduleLayer(modulePath(repository)), ConveyorPlugin.class)
            .stream()
            .map(ServiceLoader.Provider::get);
    }

    private ImmutableSet<Path> modulePath(Repository repository) {
        return ModulePath.from(
                indexed.values()
                    .stream()
                    .map(plugin -> plugin.artifact(repository))
                    .collect(new ImmutableSetCollector<>())
            )
            .resolved();
    }

    private Map<String, String> interpolatedConfiguration(String name, Properties properties) {
        return indexed.value(name)
            .map(Plugin::configuration)
            .map(configuration -> configuration.interpolated(properties))
            .orElseThrow();
    }

    private boolean isEnabled(String name) {
        return indexed.value(name)
            .map(Plugin::isEnabled)
            .orElseThrow();
    }

    private ModuleLayer moduleLayer(ImmutableSet<Path> paths) {
        var parent = getClass().getModule().getLayer();
        return parent.defineModulesWithOneLoader(
            parent.configuration()
                .resolveAndBind(
                    ModuleFinder.of(paths.stream().toArray(Path[]::new)),
                    ModuleFinder.of(),
                    Set.of()
                ),
            Thread.currentThread().getContextClassLoader()
        );
    }
}
