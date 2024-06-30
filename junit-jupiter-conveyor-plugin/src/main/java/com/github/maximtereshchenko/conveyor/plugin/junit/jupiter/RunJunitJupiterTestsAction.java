package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

final class RunJunitJupiterTestsAction implements Supplier<Optional<Path>> {

    private final Path classesDirectory;
    private final Path testClassesDirectory;
    private final Set<Path> dependencies;

    RunJunitJupiterTestsAction(
        Path classesDirectory,
        Path testClassesDirectory,
        Set<Path> dependencies
    ) {
        this.classesDirectory = classesDirectory;
        this.testClassesDirectory = testClassesDirectory;
        this.dependencies = dependencies;
    }

    @Override
    public Optional<Path> get() {
        if (Files.exists(testClassesDirectory)) {
            runWithContextClassLoader(classLoader(classpath()), this::executeTests);
        }
        return Optional.empty();
    }

    private Set<Path> classpath() {
        var classpath = new HashSet<>(dependencies);
        classpath.add(classesDirectory);
        classpath.add(testClassesDirectory);
        return classpath;
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
