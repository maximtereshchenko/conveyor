package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.ManualDefinition;

final class DefinedManual implements Manual {

    private final Manual template;
    private final ManualDefinition manualDefinition;
    private final Repository repository;

    DefinedManual(Manual template, ManualDefinition manualDefinition, Repository repository) {
        this.template = template;
        this.manualDefinition = manualDefinition;
        this.repository = repository;
    }

    @Override
    public Properties properties() {
        return template.properties().override(new Properties(manualDefinition.properties()));
    }

    @Override
    public Plugins plugins() {
        return template.plugins()
            .override(
                Plugins.from(
                    manualDefinition.plugins()
                        .stream()
                        .map(dependency -> new DefinedPlugin(dependency, repository, properties()))
                        .collect(new ImmutableSetCollector<>())
                )
            );
    }

    @Override
    public Dependencies dependencies(SchematicProducts schematicProducts) {
        return template.dependencies(schematicProducts)
            .with(
                Dependencies.from(
                    manualDefinition.dependencies()
                        .stream()
                        .map(dependency -> new DefinedDependency(dependency, repository))
                        .collect(new ImmutableSetCollector<>())
                )
            );
    }
}
