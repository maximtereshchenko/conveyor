package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;

import java.util.Optional;

interface Template {

    Repositories repositories();

    SchematicProperties schematicProperties();

    Properties properties(Repositories repositories);

    Plugins plugins(Repositories repositories);

    Dependencies dependencies(Repositories repositories, SchematicProducts schematicProducts);

    Optional<Schematic> root();

    boolean inheritsFrom(Schematic schematic);
}
