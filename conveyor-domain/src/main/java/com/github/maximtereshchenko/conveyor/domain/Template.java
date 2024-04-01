package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;

import java.util.Optional;

interface Template {

    Optional<Repository> repository();

    Properties properties(Repository repository);

    Plugins plugins(Repository repository);

    Dependencies dependencies(Repository repository, SchematicProducts schematicProducts);

    Optional<Schematic> root();

    boolean inheritsFrom(Schematic schematic);
}
