package com.github.maximtereshchenko.conveyor.api;

import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Products;

import java.nio.file.Path;
import java.util.*;

public final class SchematicProducts {

    private final Map<String, Products> products;

    private SchematicProducts(Map<String, Products> products) {
        this.products = Map.copyOf(products);
    }

    public SchematicProducts() {
        this(Map.of());
    }

    public SchematicProducts with(String schematic, Products products) {
        var copy = new HashMap<>(this.products);
        copy.put(schematic, products);
        return new SchematicProducts(copy);
    }

    public Collection<Path> byType(String schematic, ProductType type) {
        var found = products.get(schematic);
        if (found == null) {
            return List.of();
        }
        return found.byType(type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (SchematicProducts) object;
        return Objects.equals(products, that.products);
    }
}
