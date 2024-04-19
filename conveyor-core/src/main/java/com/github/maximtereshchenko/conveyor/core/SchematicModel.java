package com.github.maximtereshchenko.conveyor.core;

import java.util.LinkedHashSet;
import java.util.Set;

interface SchematicModel {

    Id id();

    SemanticVersion version();

    TemplateModel template();

    PropertiesModel properties();

    PreferencesModel preferences();

    LinkedHashSet<PluginModel> plugins();

    Set<DependencyModel> dependencies();
}
