package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.DependencyScope;
import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.ProductType;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public interface ConveyorSchematic {

    Path discoveryDirectory();

    Path constructionDirectory();

    Optional<String> propertyValue(String key);

    Set<Path> modulePath(Set<DependencyScope> scopes);

    Product product(Path path, ProductType type);
}
