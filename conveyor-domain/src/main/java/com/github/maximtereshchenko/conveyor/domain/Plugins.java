package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProperties;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematicDependencies;

final class Plugins {

    private final ImmutableMap<String, Plugin> byName;

    private Plugins(ImmutableMap<String, Plugin> byName) {
        this.byName = byName;
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

    ConveyorPlugins conveyorPlugins(
        ConveyorProperties conveyorProperties,
        ConveyorSchematicDependencies conveyorSchematicDependencies
    ) {
        return new ConveyorPlugins(
            this,
            ModulePath.from(byName.values()).modulePath(),
            conveyorProperties,
            conveyorSchematicDependencies
        );
    }

    Configuration configuration(String name) {
        return byName.value(name)
            .map(Plugin::configuration)
            .orElseThrow();
    }

    Plugins override(Plugins plugins) {
        return new Plugins(
            plugins.byName.stream()
                .reduce(
                    byName,
                    (current, entry) -> current.compute(
                        entry.getKey(),
                        entry::getValue,
                        plugin -> new OverriddenPlugin(plugin, entry.getValue())
                    ),
                    new PickSecond<>()
                )
        );
    }
}
