package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.Set;

interface SchematicModel {

    String group();

    String name();

    SemanticVersion version();

    TemplateModel template();

    Map<String, String> properties();

    PreferencesModel preferences();

    Set<PluginModel> plugins();

    Set<DependencyModel> dependencies();
}
