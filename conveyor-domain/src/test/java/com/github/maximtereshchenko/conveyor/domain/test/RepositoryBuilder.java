package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.JacksonAdapter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

final class RepositoryBuilder {

    private final JacksonAdapter gsonAdapter;
    private final Collection<Installation> installations;

    private RepositoryBuilder(JacksonAdapter gsonAdapter, Collection<Installation> installations) {
        this.gsonAdapter = gsonAdapter;
        this.installations = List.copyOf(installations);
    }

    RepositoryBuilder(JacksonAdapter gsonAdapter) {
        this(gsonAdapter, List.of());
    }

    RepositoryBuilder superManual() {
        return superManual(builder -> builder);
    }

    RepositoryBuilder superManual(UnaryOperator<ManualBuilder> configuration) {
        return manual(builder ->
            configuration.apply(
                builder.name("super-manual")
                    .version("1.0.0")
                    .noTemplate()
            )
        );
    }

    RepositoryBuilder manual(UnaryOperator<ManualBuilder> configuration) {
        var copy = new ArrayList<>(installations);
        copy.add(path -> configuration.apply(new ManualBuilder(gsonAdapter)).install(path));
        return new RepositoryBuilder(gsonAdapter, copy);
    }

    RepositoryBuilder jar(String templateDirectory, UnaryOperator<JarBuilder> configuration) {
        var copy = new ArrayList<>(installations);
        copy.add(path ->
            configuration.apply(JarBuilder.from(templateDirectory))
                .install(path)
        );
        return new RepositoryBuilder(gsonAdapter, copy);
    }

    void install(Path path) {
        installations.forEach(installation -> installation.install(path));
    }

    @FunctionalInterface
    private interface Installation {

        void install(Path path);
    }
}
