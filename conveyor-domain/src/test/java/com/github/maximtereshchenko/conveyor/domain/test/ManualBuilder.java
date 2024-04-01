package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.api.port.*;
import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.gson.JacksonAdapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

final class ManualBuilder {

    private final JacksonAdapter gsonAdapter;
    private final ManualDefinition manualDefinition;

    private ManualBuilder(JacksonAdapter gsonAdapter, ManualDefinition manualDefinition) {
        this.gsonAdapter = gsonAdapter;
        this.manualDefinition = manualDefinition;
    }

    ManualBuilder(JacksonAdapter gsonAdapter) {
        this(
            gsonAdapter,
            new ManualDefinition(
                "",
                0,
                new ManualTemplateDefinition("super-manual", 1),
                Map.of(),
                new PreferencesDefinition(),
                List.of(),
                List.of()
            )
        );
    }

    ManualBuilder noTemplate() {
        return template(new NoExplicitlyDefinedTemplate());
    }

    ManualBuilder name(String name) {
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                name,
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                manualDefinition.preferences(),
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
                manualDefinition.preferences(),
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
                manualDefinition.preferences(),
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
    }

    ManualBuilder plugin(String name, int version, Map<String, String> configuration) {
        var copy = new ArrayList<>(manualDefinition.plugins());
        copy.add(new PluginDefinition(name, Optional.of(version), configuration));
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                manualDefinition.preferences(),
                copy,
                manualDefinition.dependencies()
            )
        );
    }

    ManualBuilder dependency(String name, int version, DependencyScope scope) {
        var copy = new ArrayList<>(manualDefinition.dependencies());
        copy.add(new ManualDependencyDefinition(name, version, scope));
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                manualDefinition.preferences(),
                manualDefinition.plugins(),
                copy
            )
        );
    }

    void install(Path directory) {
        try {
            gsonAdapter.write(
                Files.createDirectories(directory)
                    .resolve("%s-%d.json".formatted(manualDefinition.name(), manualDefinition.version())),
                manualDefinition
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ManualBuilder template(TemplateForManualDefinition templateDefinition) {
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                templateDefinition,
                manualDefinition.properties(),
                manualDefinition.preferences(),
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
    }
}
