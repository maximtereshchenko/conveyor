package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.port.SchematicDefinitionTranslator;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class ConveyorFacade implements ConveyorModule {

    private final Http http;
    private final SchematicModelFactory modelFactory;
    private final ModulePathFactory modulePathFactory;
    private final SchematicDefinitionFactory schematicDefinitionFactory;
    private final SchematicDefinitionTranslator schematicDefinitionTranslator;

    public ConveyorFacade(SchematicDefinitionTranslator schematicDefinitionTranslator) {
        this.http = new Http();
        this.modelFactory = new SchematicModelFactory(schematicDefinitionTranslator);
        this.modulePathFactory = new ModulePathFactory();
        this.schematicDefinitionFactory = new SchematicDefinitionFactory(XmlFactory.newInstance());
        this.schematicDefinitionTranslator = schematicDefinitionTranslator;
    }

    @Override
    public void construct(Path path, Stage stage) {
        schematics(path).construct(stage);
    }

    private Schematics schematics(Path path) {
        var schematics = modelFactory.extendableLocalInheritanceHierarchyModels(path)
            .stream()
            .map(extendableLocalInheritanceHierarchyModel ->
                new Schematic(
                    extendableLocalInheritanceHierarchyModel,
                    http,
                    modulePathFactory,
                    schematicDefinitionFactory,
                    schematicDefinitionTranslator,
                    modelFactory
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
