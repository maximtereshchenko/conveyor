package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.Product;

import java.util.Set;

public interface ConveyorTask {

    Set<Product> execute(ConveyorSchematic schematic, Set<Product> products);
}
