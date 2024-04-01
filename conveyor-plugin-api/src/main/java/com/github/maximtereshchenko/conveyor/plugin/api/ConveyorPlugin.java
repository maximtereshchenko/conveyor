package com.github.maximtereshchenko.conveyor.plugin.api;

import java.util.Collection;
import java.util.Map;

public interface ConveyorPlugin {

    String name();

    Collection<ConveyorTaskBinding> bindings(ConveyorProject project, Map<String, String> configuration);
}
