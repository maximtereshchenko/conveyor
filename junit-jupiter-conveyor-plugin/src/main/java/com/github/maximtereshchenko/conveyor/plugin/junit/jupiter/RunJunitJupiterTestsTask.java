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

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
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
        product(products, ProductType.EXPLODED_TEST_MODULE)
            .ifPresent(explodedTestModule -> executeTests(explodedTestModule, products));
        return Set.of();
    }

    private void executeTests(Path explodedTestModule, Set<Product> products) {
        var testModule = testModule(
            moduleLayer(modulePath(explodedTestModule, products)),
            explodedTestModule
        );
        runWithContextClassLoader(testModule.getClassLoader(), () -> executeTests(testModule));
    }

    private Set<Path> modulePath(Path explodedTestModule, Set<Product> products) {
        return Stream.of(
                schematic.modulePath(Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST))
                    .stream(),
                product(products, ProductType.EXPLODED_MODULE).stream(),
                Stream.of(explodedTestModule)
            )
            .flatMap(Function.identity())
            .collect(Collectors.toSet());
    }

    private void executeTests(Module testModule) {
        var failureTestExecutionListener = new FailureTestExecutionListener();
        LauncherFactory.create()
            .execute(
                launcherDiscoveryRequest(testModule),
                new ReportingTestExecutionListener(),
                failureTestExecutionListener
            );
        if (!failureTestExecutionListener.isSuccess()) {
            throw new IllegalArgumentException();
        }
    }

    private LauncherDiscoveryRequest launcherDiscoveryRequest(Module testModule) {
        var request = LauncherDiscoveryRequestBuilder.request();
        for (var packageName : testModule.getPackages()) {
            request.selectors(DiscoverySelectors.selectPackage(packageName));
        }
        return request.build();
    }

    private Module testModule(ModuleLayer moduleLayer, Path explodedTestModule) {
        return moduleLayer.findModule(
                ModuleFinder.of(explodedTestModule)
                    .findAll()
                    .iterator()
                    .next()
                    .descriptor()
                    .name()
            )
            .orElseThrow();
    }

    private Optional<Path> product(Set<Product> products, ProductType explodedModule) {
        return products.stream()
            .filter(product -> product.schematicCoordinates().equals(schematic.coordinates()))
            .filter(product -> product.type() == explodedModule)
            .map(Product::path)
            .findAny();
    }

    private ModuleLayer moduleLayer(Set<Path> paths) {
        var parent = getClass().getModule().getLayer();
        var moduleFinder = ModuleFinder.of(paths.toArray(Path[]::new));
        return parent.defineModulesWithOneLoader(
            parent.configuration()
                .resolveAndBind(
                    ModuleFinder.of(),
                    moduleFinder,
                    moduleFinder.findAll()
                        .stream()
                        .map(ModuleReference::descriptor)
                        .map(ModuleDescriptor::name)
                        .collect(Collectors.toSet())
                ),
            getClass().getClassLoader()
        );
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
