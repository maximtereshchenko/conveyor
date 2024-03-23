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
                "",
                new ManualTemplateDefinition("super-manual", "1.0.0"),
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

    ManualBuilder version(String version) {
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

    ManualBuilder plugin(String name, String version, Map<String, String> configuration) {
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

    ManualBuilder dependency(String name, String version, DependencyScope scope) {
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
                    .resolve(
                        "%s-%s.json".formatted(
                            manualDefinition.name(),
                            manualDefinition.version()
                        )
                    ),
                manualDefinition
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    ManualBuilder preference(String name, String version) {
        var copy = new ArrayList<>(manualDefinition.preferences().artifacts());
        copy.add(new ArtifactPreferenceDefinition(name, version));
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                new PreferencesDefinition(manualDefinition.preferences().inclusions(), copy),
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
    }

    ManualBuilder preferenceInclusion(String name, String version) {
        var copy = new ArrayList<>(manualDefinition.preferences().inclusions());
        copy.add(new PreferencesInclusionDefinition(name, version));
        return new ManualBuilder(
            gsonAdapter,
            new ManualDefinition(
                manualDefinition.name(),
                manualDefinition.version(),
                manualDefinition.template(),
                manualDefinition.properties(),
                new PreferencesDefinition(copy, manualDefinition.preferences().artifacts()),
                manualDefinition.plugins(),
                manualDefinition.dependencies()
            )
        );
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
