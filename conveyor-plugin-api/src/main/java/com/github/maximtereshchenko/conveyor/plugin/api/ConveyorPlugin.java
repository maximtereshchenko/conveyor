package com.github.maximtereshchenko.conveyor.plugin.api;

import java.util.Collection;

public interface ConveyorPlugin {

    String name();

    Collection<ConveyorTaskBinding> bindings(ConveyorPluginConfiguration configuration);
}
