package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RunJunitJupiterTestsTask implements ConveyorTask {

    private final ConveyorSchematic schematic;

    RunJunitJupiterTestsTask(ConveyorSchematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        product(products, ProductType.EXPLODED_TEST_JAR)
            .ifPresent(explodedTestJar -> executeTests(explodedTestJar, products));
        return Set.of();
    }

    private void executeTests(Path explodedTestJar, Set<Product> products) {
        runWithContextClassLoader(
            classLoader(classPath(explodedTestJar, products)),
            () -> executeTests(explodedTestJar)
        );
    }

    private Set<Path> classPath(Path explodedTestJar, Set<Product> products) {
        return Stream.of(
                schematic.classPath(Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST))
                    .stream(),
                product(products, ProductType.EXPLODED_JAR).stream(),
                Stream.of(explodedTestJar)
            )
            .flatMap(Function.identity())
            .collect(Collectors.toSet());
    }

    private void executeTests(Path explodedTestJar) {
        var failureTestExecutionListener = new FailureTestExecutionListener();
        LauncherFactory.create()
            .execute(
                launcherDiscoveryRequest(explodedTestJar),
                new ReportingTestExecutionListener(),
                failureTestExecutionListener
            );
        if (!failureTestExecutionListener.isSuccess()) {
            throw new IllegalArgumentException();
        }
    }

    private LauncherDiscoveryRequest launcherDiscoveryRequest(Path explodedTestJar) {
        return LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClasspathRoots(Set.of(explodedTestJar)))
            .build();
    }

    private Optional<Path> product(Set<Product> products, ProductType explodedModule) {
        return products.stream()
            .filter(product -> product.schematicCoordinates().equals(schematic.coordinates()))
            .filter(product -> product.type() == explodedModule)
            .map(Product::path)
            .findAny();
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
