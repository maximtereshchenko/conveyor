package com.github.maximtereshchenko.conveyor.plugin.compile.test;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@ExtendWith(JimfsExtension.class)
abstract class CompilePluginTest {

    Path constructionDirectory(Path path) {
        return path.resolve(".conveyor");
    }

    Path srcMainJava(Path path) {
        return path.resolve("src").resolve("main").resolve("java");
    }

    Path explodedModule(Path path) {
        return constructionDirectory(path).resolve("exploded-module");
    }

    Path moduleInfoJava(Path path) {
        return path.resolve("module-info.java");
    }

    Path moduleInfoClass(Path path) {
        return path.resolve("module-info.class");
    }

    List<ConveyorTaskBinding> bindings(Path path) {
        return bindings(path, constructionDirectory(path), Set.of());
    }

    List<ConveyorTaskBinding> bindings(
        Path discoveryDirectory,
        Path constructionDirectory,
        Set<Path> dependencies
    ) {
        var schematic = new FakeConveyorSchematic(
            discoveryDirectory,
            constructionDirectory,
            dependencies
        );
        return ServiceLoader.load(ConveyorPlugin.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(conveyorPlugin -> conveyorPlugin.bindings(schematic, Map.of()))
            .flatMap(Collection::stream)
            .toList();
    }

    Set<Product> executeTasks(Path path, Path... dependencies) {
        var products = new HashSet<Product>();
        for (var binding : bindings(path, constructionDirectory(path), Set.of(dependencies))) {
            products.addAll(binding.task().execute(products));
        }
        return products;
    }

    void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
