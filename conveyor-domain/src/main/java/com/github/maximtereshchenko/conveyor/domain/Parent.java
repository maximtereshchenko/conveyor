package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.DependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import java.util.Collection;
import java.util.Map;

interface Parent {

    Collection<PluginDefinition> plugins();

    Collection<DependencyDefinition> dependencies();

    Map<String, String> properties();
}
