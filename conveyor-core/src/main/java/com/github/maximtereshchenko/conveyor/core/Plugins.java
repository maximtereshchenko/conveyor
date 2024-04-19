package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

final class Plugins {

    private final LinkedHashSet<Plugin> all;
    private final ModulePathFactory modulePathFactory;

    Plugins(LinkedHashSet<Plugin> all, ModulePathFactory modulePathFactory) {
        this.all = all;
        this.modulePathFactory = modulePathFactory;
    }

    Set<Product> executeTasks(
        ConveyorSchematic conveyorSchematic,
        Set<Product> products,
        Stage stage
    ) {
        var copy = new HashSet<>(products);
        for (var task : tasks(conveyorSchematic, stage)) {
            copy.addAll(task.execute(copy));
        }
        return copy;
    }

    private List<ConveyorTask> tasks(ConveyorSchematic conveyorSchematic, Stage stage) {
        return conveyorPlugins()
            .filter(conveyorPlugin -> named(conveyorPlugin.name()).isEnabled())
            .sorted(this::byPosition)
            .flatMap(conveyorPlugin -> bindings(conveyorSchematic, conveyorPlugin))
            .filter(binding -> isBeforeOrEqual(binding, stage))
            .sorted(byStageAndStep())
            .map(ConveyorTaskBinding::task)
            .toList();
    }

    private int byPosition(ConveyorPlugin first, ConveyorPlugin second) {
        for (var plugin : all) {
            var name = plugin.id().name();
            if (name.equals(first.name())) {
                return -1;
            }
            if (name.equals(second.name())) {
                return 1;
            }
        }
        return 0;
    }

    private Comparator<ConveyorTaskBinding> byStageAndStep() {
        return Comparator.comparing(ConveyorTaskBinding::stage).thenComparing(ConveyorTaskBinding::step);
    }

    private boolean isBeforeOrEqual(ConveyorTaskBinding conveyorTaskBinding, Stage stage) {
        return conveyorTaskBinding.stage().compareTo(stage) <= 0;
    }

    private Stream<ConveyorTaskBinding> bindings(
        ConveyorSchematic conveyorSchematic,
        ConveyorPlugin conveyorPlugin
    ) {
        return conveyorPlugin.bindings(
                conveyorSchematic,
                named(conveyorPlugin.name()).configuration()
            )
            .stream();
    }

    private Stream<ConveyorPlugin> conveyorPlugins() {
        return ServiceLoader.load(
                moduleLayer(modulePathFactory.modulePath(all)),
                ConveyorPlugin.class
            )
            .stream()
            .map(ServiceLoader.Provider::get);
    }

    private Plugin named(String name) {
        return all.stream()
            .filter(plugin -> plugin.id().name().equals(name))
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
