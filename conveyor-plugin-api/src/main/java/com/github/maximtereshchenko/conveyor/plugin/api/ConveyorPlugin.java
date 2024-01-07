package com.github.maximtereshchenko.conveyor.plugin.api;

import java.util.Collection;

public interface ConveyorPlugin {

    Collection<ConveyorTask> tasks(ConveyorPluginConfiguration configuration);
}
