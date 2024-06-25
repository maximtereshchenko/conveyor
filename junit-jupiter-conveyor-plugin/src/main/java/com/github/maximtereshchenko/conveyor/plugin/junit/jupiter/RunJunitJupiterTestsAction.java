package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RunJunitJupiterTestsAction implements Supplier<Optional<Path>> {

    private final Path testClassesDirectory;
    private final Path classesDirectory;
    private final ConveyorSchematic schematic;

    RunJunitJupiterTestsAction(
        Path testClassesDirectory,
        Path classesDirectory,
        ConveyorSchematic schematic
    ) {
        this.testClassesDirectory = testClassesDirectory;
        this.classesDirectory = classesDirectory;
        this.schematic = schematic;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(testClassesDirectory)) {
            runWithContextClassLoader(classLoader(classpath()), this::executeTests);
        }
        return Optional.empty();
    }

    private Set<Path> classpath() {
        return Stream.of(
                schematic.classpath(Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST)),
                Set.of(classesDirectory),
                Set.of(testClassesDirectory)
            )
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    private void executeTests() {
        var failureTestExecutionListener = new FailureTestExecutionListener();
        LauncherFactory.create()
            .execute(
                launcherDiscoveryRequest(),
                new ReportingTestExecutionListener(),
                failureTestExecutionListener
            );
        if (!failureTestExecutionListener.isSuccess()) {
            throw new IllegalArgumentException();
        }
    }

    private LauncherDiscoveryRequest launcherDiscoveryRequest() {
        return LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClasspathRoots(Set.of(testClassesDirectory)))
            .build();
    }

    private ClassLoader classLoader(Set<Path> paths) {
        return URLClassLoader.newInstance(
            paths.stream()
                .map(this::url)
                .toArray(URL[]::new),
            getClass().getClassLoader()
        );
    }

    private URL url(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void runWithContextClassLoader(ClassLoader classLoader, Runnable runnable) {
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
}
