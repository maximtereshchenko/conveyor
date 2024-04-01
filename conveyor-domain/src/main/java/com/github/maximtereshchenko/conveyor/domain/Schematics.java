package com.github.maximtereshchenko.conveyor.domain;

import com.github.maximtereshchenko.conveyor.api.SchematicProducts;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.util.LinkedHashSet;

final class Schematics {

    private final LinkedHashSet<Schematic> all;
    private final Schematic initial;

    Schematics(LinkedHashSet<Schematic> all, Schematic initial) {
        this.all = all;
        this.initial = initial;
    }

    boolean haveDependencyBetween(String name, Schematic schematic) {
        return named(name).dependsOn(schematic, this);
    }

    SchematicProducts construct(Stage stage) {
        return all.stream()
            .filter(this::toBeConstructed)
            .sorted(this::comparedByMutualRequirement)
            .reduce(
                new SchematicProducts(),
                (schematicProducts, schematic) -> schematic.construct(schematicProducts, stage(stage, schematic)),
                (a, b) -> a
            );
    }

    private Schematic named(String name) {
        return all.stream()
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
        return all.stream()
            .anyMatch(other -> other.dependsOn(schematic, this));
    }

    private int comparedByMutualRequirement(Schematic first, Schematic second) {
        if (requirementExistsBetween(first, second)) {
            return 1;
        }
        if (requirementExistsBetween(second, first)) {
            return -1;
        }
        return 0;
    }

    private boolean toBeConstructed(Schematic schematic) {
        return requirementExistsBetween(initial, schematic) || schematic.inheritsFrom(initial);
    }

    private boolean requirementExistsBetween(Schematic from, Schematic to) {
        return from.inheritsFrom(to) || from.dependsOn(to, this);
    }
}
