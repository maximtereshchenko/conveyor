package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

final class CopyResourcesTask implements ConveyorTask {

    private final Path path;
    private final ProductType productType;

    CopyResourcesTask(Path path, ProductType productType) {
        this.path = path;
        this.productType = productType;
    }

    @Override
    public Set<Product> execute(Set<Product> products) {
        if (Files.exists(path)) {
            copy(target(products));
        }
        return Set.of();
    }

    private void copy(Path target) {
        try {
            Files.walkFileTree(path, new CopyRecursively(path, target));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path target(Set<Product> products) {
        return products.stream()
            .filter(product -> product.type() == productType)
            .map(Product::path)
            .findAny()
            .orElseThrow();
    }
}
