package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.Products;

public interface ConveyorTask {

    Products execute(ConveyorSchematicDependencies dependencies, Products products);
}
