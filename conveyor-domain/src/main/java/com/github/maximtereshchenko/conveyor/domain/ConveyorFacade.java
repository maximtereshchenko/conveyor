package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final DefinitionReader definitionReader;
    private final ModelFactory modelFactory;
    private final ModulePathFactory modulePathFactory;

    public ConveyorFacade(DefinitionReader definitionReader) {
        this.definitionReader = definitionReader;
        this.modelFactory = new ModelFactory(definitionReader);
        this.modulePathFactory = new ModulePathFactory();
    }

    @Override
    public SchematicProducts construct(Path path, Stage stage) {
        return schematics(path).construct(stage);
    }

    private Schematics schematics(Path path) {
        var schematics = modelFactory.partialSchematicHierarchies(path)
            .stream()
            .map(partialSchematicHierarchy ->
                new Schematic(
                    partialSchematicHierarchy,
                    definitionReader,
                    modelFactory,
                    modulePathFactory
                )
            )
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Schematics(schematics, initial(schematics, path));
    }

    private Schematic initial(LinkedHashSet<Schematic> schematics, Path path) {
        return schematics.stream()
            .filter(schematic -> schematic.locatedAt(path))
            .findAny()
            .orElseThrow();
    }
}
