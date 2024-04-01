package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ManualBuilder {

    private final GsonAdapter gsonAdapter;
    private final ManualDefinition manualDefinition;

    private ManualBuilder(GsonAdapter gsonAdapter, ManualDefinition manualDefinition) {
        this.gsonAdapter = gsonAdapter;
        this.manualDefinition = manualDefinition;
    }

    ManualBuilder(GsonAdapter gsonAdapter) {
        this(
            gsonAdapter,
            new ManualDefinition(
                "",
                0,
                new ManualTemplateDefinition("super-manual", 1),
                Map.of(),
                List.of(),
                List.of()
            )
        );
    }

    ManualBuilder noTemplate() {
        return template(new NoExplicitTemplate());
    }

    ManualBuilder template(String name, int version) {
        return template(new ManualTemplateDefinition(name, version));
    }

    ManualBuilder name(String name) {
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                name,
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
    }

    ManualBuilder version(int version) {
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                version,
                manualDefinition.template(),
                manualDefinition.properties(),
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
    }

    ManualBuilder property(String key, String value) {
        var copy = new HashMap<>(manualDefinition.properties());
        copy.put(key, value);
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                manualDefinition.template(),
                copy,
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
    }

    ManualBuilder plugin(String name, int version, Map<String, String> configuration) {
        var copy = new ArrayList<>(manualDefinition.plugins());
        copy.add(new PluginDefinition(name, version, configuration));
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                copy,
                manualDefinition.dependencies()
            )
        );
    }

    ManualBuilder dependency(String name, int version, DependencyScope scope) {
        var copy = new ArrayList<>(manualDefinition.dependencies());
        copy.add(new ArtifactDependencyDefinition(name, version, scope));
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                manualDefinition.plugins(),
                copy
            )
        );
    }

    void install(Path directory) {
        gsonAdapter.write(
            directory.resolve("%s-%d.json".formatted(manualDefinition.name(), manualDefinition.version())),
            manualDefinition
        );
    }

    private ManualBuilder template(TemplateDefinition templateDefinition) {
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                templateDefinition,
                manualDefinition.properties(),
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
    }
}
