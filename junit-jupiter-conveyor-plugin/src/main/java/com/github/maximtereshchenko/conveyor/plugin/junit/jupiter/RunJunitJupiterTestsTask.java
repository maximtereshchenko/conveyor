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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class RunJunitJupiterTestsTask implements ConveyorTask {

    private final ConveyorSchematic schematic;

    RunJunitJupiterTestsTask(ConveyorSchematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        var explodedTestModule = product(products, ProductType.EXPLODED_TEST_MODULE);
        var testModule = testModule(
            moduleLayer(
                modulePath(
                    product(products, ProductType.EXPLODED_MODULE),
                    explodedTestModule
                )
            ),
            explodedTestModule
        );
        runWithContextClassLoader(testModule.getClassLoader(), () -> executeTests(testModule));
        return Set.of();
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

    private HashSet<Path> modulePath(Path explodedModule, Path explodedTestModule) {
        var paths = new HashSet<>(
            schematic.modulePath(Set.of(DependencyScope.IMPLEMENTATION, DependencyScope.TEST))
        );
        paths.add(explodedModule);
        paths.add(explodedTestModule);
        return paths;
    }

    private Path product(Set<Product> products, ProductType explodedModule) {
        return products.stream()
            .filter(product -> product.type() == explodedModule)
            .map(Product::path)
            .findAny()
            .orElseThrow();
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
            Thread.currentThread().getContextClassLoader()
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
