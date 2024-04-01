package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.common.api.Products;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematicDependencies;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.Set;

final class ConveyorPlugins {

    private final Plugins plugins;
    private final ImmutableSet<Path> paths;
    private final ConveyorProperties conveyorProperties;
    private final ConveyorSchematicDependencies conveyorSchematicDependencies;

    ConveyorPlugins(
        Plugins plugins,
        ImmutableSet<Path> paths,
        ConveyorProperties conveyorProperties,
        ConveyorSchematicDependencies conveyorSchematicDependencies
    ) {
        this.plugins = plugins;
        this.paths = paths;
        this.conveyorProperties = conveyorProperties;
        this.conveyorSchematicDependencies = conveyorSchematicDependencies;
    }

    Products executeTasks(Stage stage) {
        return ServiceLoader.load(moduleLayer(), ConveyorPlugin.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(conveyorPlugin ->
                conveyorPlugin.bindings(
                    conveyorProperties,
                    plugins.configuration(conveyorPlugin.name()).interpolated()
                )
            )
            .flatMap(Collection::stream)
            .filter(binding -> binding.stage().compareTo(stage) <= 0)
            .sorted(Comparator.comparing(ConveyorTaskBinding::stage).thenComparing(ConveyorTaskBinding::step))
            .map(ConveyorTaskBinding::task)
            .reduce(
                new Products(),
                (products, task) -> task.execute(conveyorSchematicDependencies, products),
                new PickSecond<>()
            );
    }

    private ModuleLayer moduleLayer() {
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
