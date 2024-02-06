package com.github.maximtereshchenko.conveyor.common.api;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Products {

    private final Map<Path, ProductType> files;

    private Products(Map<Path, ProductType> files) {
        this.files = Map.copyOf(files);
    }

    public Products() {
        this(Map.of());
    }

    public Set<Path> byType(ProductType type) {
        return files.entrySet()
            .stream()
            .filter(entry -> entry.getValue() == type)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    public Products with(Path path, ProductType type) {
        var copy = new HashMap<>(files);
        copy.put(path, type);
        return new Products(copy);
    }
}
