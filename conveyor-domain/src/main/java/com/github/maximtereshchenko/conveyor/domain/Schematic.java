package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

interface Schematic extends Manual {

    String name();

    SchematicProducts construct(SchematicProducts schematicProducts, Stage stage);

    ImmutableList<Schematic> inclusions();

    boolean dependsOn(Schematic schematic);
}
