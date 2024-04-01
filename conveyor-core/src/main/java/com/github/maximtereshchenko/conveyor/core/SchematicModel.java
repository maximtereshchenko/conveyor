package com.github.maximtereshchenko.conveyor.core;

import java.util.Set;

interface SchematicModel {

    Id id();

    SemanticVersion version();

    TemplateModel template();

    PropertiesModel properties();

    PreferencesModel preferences();

    Set<PluginModel> plugins();

    Set<DependencyModel> dependencies();
}
