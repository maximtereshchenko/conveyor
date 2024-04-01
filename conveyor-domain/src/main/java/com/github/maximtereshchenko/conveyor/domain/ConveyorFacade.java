package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionTranslator;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final SchematicDefinitionTranslator schematicDefinitionTranslator;
    private final SchematicModelFactory modelFactory;
    private final ModulePathFactory modulePathFactory;
    private final XmlFactory xmlFactory;
    private final Http http;

    public ConveyorFacade(SchematicDefinitionTranslator schematicDefinitionTranslator) {
        this.schematicDefinitionTranslator = schematicDefinitionTranslator;
        this.modelFactory = new SchematicModelFactory(schematicDefinitionTranslator);
        this.modulePathFactory = new ModulePathFactory();
        this.xmlFactory = XmlFactory.newInstance();
        this.http = new Http();
    }

    @Override
    public void construct(Path path, Stage stage) {
        schematics(path).construct(stage);
    }

    private Schematics schematics(Path path) {
        var schematics = modelFactory.hierarchicalLocalSchematicModels(path)
            .stream()
            .map(hierarchicalLocalSchematicModel ->
                new Schematic(
                    hierarchicalLocalSchematicModel,
                    schematicDefinitionTranslator,
                    modelFactory,
                    modulePathFactory,
                    xmlFactory,
                    http
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
