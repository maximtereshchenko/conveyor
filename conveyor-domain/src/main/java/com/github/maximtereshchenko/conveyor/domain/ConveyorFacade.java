package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionTranslator;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final DefinitionTranslator definitionTranslator;
    private final ModelFactory modelFactory;
    private final ModulePathFactory modulePathFactory;
    private final XmlFactory xmlFactory;

    public ConveyorFacade(DefinitionTranslator definitionTranslator) {
        this.definitionTranslator = definitionTranslator;
        this.modelFactory = new ModelFactory(definitionTranslator);
        this.modulePathFactory = new ModulePathFactory();
        this.xmlFactory = XmlFactory.newInstance();
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
                    definitionTranslator,
                    modelFactory,
                    modulePathFactory,
                    xmlFactory
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
