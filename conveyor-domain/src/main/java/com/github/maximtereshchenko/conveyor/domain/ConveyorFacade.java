package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.ConveyorModule;
import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.api.port.DefinitionReader;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.nio.file.Path;
import java.util.stream.Stream;

public final class ConveyorFacade implements ConveyorModule {

    private final DefinitionReader definitionReader;

    public ConveyorFacade(DefinitionReader definitionReader) {
        this.definitionReader = definitionReader;
    }

    @Override
    public SchematicProducts construct(Path path, Stage stage) {
        return schematics(Schematic.from(definitionReader, path))
            .reduce(
                new SchematicProducts(),
                (aggregated, schematic) -> schematic.construct(schematic.repository().orElseThrow(), aggregated, stage),
                new PickSecond<>()
            );
    }

    private Stream<Schematic> schematics(Schematic schematic) {
        return Stream.concat(
                Stream.of(schematic),
                schematic.inclusions()
                    .stream()
                    .flatMap(this::schematics)
            )
            .sorted(this::comparedByMutualDependency);
    }

    private int comparedByMutualDependency(Schematic first, Schematic second) {
        if (first.dependsOn(second)) {
            return 1;
        }
        if (second.dependsOn(first)) {
            return -1;
        }
        return 0;
    }
}
