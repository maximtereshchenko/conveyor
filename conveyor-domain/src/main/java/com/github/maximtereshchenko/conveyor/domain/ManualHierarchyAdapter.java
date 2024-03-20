package com.github.maximtereshchenko.conveyor.domain;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

final class ManualHierarchyAdapter implements SchematicModel<TemplateModel> {

    private final ManualHierarchy manualHierarchy;

    ManualHierarchyAdapter(ManualHierarchy manualHierarchy) {
        this.manualHierarchy = manualHierarchy;
    }

    @Override
    public String name() {
        return manualHierarchy.name();
    }

    @Override
    public int version() {
        return manualHierarchy.version();
    }

    @Override
    public TemplateModel template() {
        return manualHierarchy.template();
    }

    @Override
    public Map<String, String> properties() {
        return manualHierarchy.properties();
    }

    @Override
    public PreferencesModel preferences() {
        return manualHierarchy.preferences();
    }

    @Override
    public Set<PluginModel> plugins() {
        return manualHierarchy.plugins();
    }

    @Override
    public Set<DependencyModel> dependencies() {
        return Set.copyOf(manualHierarchy.dependencies());
    }

    @Override
    public Path path() {
        throw new IllegalArgumentException();
    }

    @Override
    public Set<RepositoryModel> repositories() {
        return Set.of();
    }
}
