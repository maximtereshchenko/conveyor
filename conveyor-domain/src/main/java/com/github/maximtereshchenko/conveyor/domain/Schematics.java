package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.util.stream.Stream;

final class Schematics {

    private final Schematic initial;
    private final ImmutableList<Schematic> hierarchy;

    Schematics(Schematic initial, ImmutableList<Schematic> hierarchy) {
        this.initial = initial;
        this.hierarchy = hierarchy;
    }

    static Schematics from(Schematic schematic) {
        return new Schematics(
            schematic,
            schematics(schematic.root().orElseThrow())
                .collect(new ImmutableListCollector<>())
        );
    }

    private static Stream<Schematic> schematics(Schematic schematic) {
        return Stream.concat(
            Stream.of(schematic),
            schematic.inclusions()
                .stream()
                .flatMap(Schematics::schematics)
        );
    }

    SchematicProducts construct(Stage stage) {
        return hierarchy.stream()
            .filter(schematic -> initial.requires(schematic, this) || initial.contains(schematic))
            .sorted(this::comparedByMutualDependency)
            .reduce(
                new SchematicProducts(),
                (schematicProducts, schematic) ->
                    schematic.construct(
                        schematic.repositories(),
                        schematicProducts,
                        stage(stage, schematic)
                    ),
                new PickSecond<>()
            );
    }

    Schematic findByName(String name) {
        return hierarchy.stream()
            .filter(schematic -> schematic.name().equals(name))
            .findAny()
            .orElseThrow();
    }

    private Stage stage(Stage stage, Schematic schematic) {
        if (stage.compareTo(Stage.ARCHIVE) >= 0) {
            return stage;
        }
        if (isDependency(schematic)) {
            return Stage.ARCHIVE;
        }
        return stage;
    }

    private boolean isDependency(Schematic schematic) {
        return hierarchy.stream()
            .anyMatch(other -> other.dependsOn(schematic, this));
    }

    private int comparedByMutualDependency(Schematic first, Schematic second) {
        if (first.requires(second, this)) {
            return 1;
        }
        if (second.requires(first, this)) {
            return -1;
        }
        return 0;
    }
}
