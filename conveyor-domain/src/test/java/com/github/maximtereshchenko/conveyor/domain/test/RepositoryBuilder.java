package com.github.maximtereshchenko.conveyor.domain.test;

import com.github.maximtereshchenko.conveyor.gson.GsonAdapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

final class RepositoryBuilder {

    private final GsonAdapter gsonAdapter;
    private final Collection<Installation> installations;

    private RepositoryBuilder(GsonAdapter gsonAdapter, Collection<Installation> installations) {
        this.gsonAdapter = gsonAdapter;
        this.installations = List.copyOf(installations);
    }

    RepositoryBuilder(GsonAdapter gsonAdapter) {
        this(gsonAdapter, List.of());
    }

    RepositoryBuilder superManual() {
        return superManual(builder -> builder);
    }

    RepositoryBuilder superManual(UnaryOperator<ManualBuilder> configuration) {
        return manual(builder ->
            configuration.apply(
                builder.name("super-manual")
                    .version(1)
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
            configuration.apply(new JarBuilder(path(templateDirectory)))
                .install(path)
        );
        return new RepositoryBuilder(gsonAdapter, copy);
    }

    RepositoryBuilder emptyJar(String name, int version) {
        var copy = new ArrayList<>(installations);
        copy.add(path -> {
            try {
                Files.createFile(path.resolve("%s-%d.jar".formatted(name, version)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return new RepositoryBuilder(gsonAdapter, copy);
    }

    void install(Path path) {
        installations.forEach(installation -> installation.install(path));
    }

    private Path path(String templateDirectory) {
        try {
            return Paths.get(
                Objects.requireNonNull(
                        Thread.currentThread()
                            .getContextClassLoader()
                            .getResource(templateDirectory)
                    )
                    .toURI()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface Installation {

        void install(Path path);
    }
}
