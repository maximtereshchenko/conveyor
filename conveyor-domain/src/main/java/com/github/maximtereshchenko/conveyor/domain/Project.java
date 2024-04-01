package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.port.ArtifactDefinition;
import com.github.maximtereshchenko.conveyor.api.port.ExternalDependencyDefinition;
import com.github.maximtereshchenko.conveyor.api.port.PluginDefinition;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

interface Project extends ArtifactDefinition {

    Map<String, String> properties();

    Collection<PluginDefinition> plugins();

    Collection<ExternalDependencyDefinition> dependencies(Set<DependencyScope> scopes);

    boolean dependsOn(String project);
}
