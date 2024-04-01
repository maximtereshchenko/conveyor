package com.github.maximtereshchenko.conveyor.domain;

import java.util.Map;
import java.util.Set;

interface Model<T extends TemplateModel, D extends DependencyModel> {

    String group();

    String name();

    SemanticVersion version();

    T template();

    Map<String, String> properties();

    PreferencesModel preferences();

    Set<PluginModel> plugins();

    Set<D> dependencies();
}
