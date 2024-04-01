package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;

interface Manual {

    Properties properties();

    Plugins plugins();

    Dependencies dependencies(SchematicProducts schematicProducts);
}
