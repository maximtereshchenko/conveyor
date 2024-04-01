package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class Schematics {

    private final LinkedHashSet<Schematic> all;
    private final Schematic initial;

    Schematics(LinkedHashSet<Schematic> all, Schematic initial) {
        this.all = all;
        this.initial = initial;
    }

    void construct(Stage stage) {
        var products = Set.<Product>of();
        for (var schematic : schematicsInContructionOrder()) {
            products = schematic.construct(products, stage(stage, schematic));
        }
    }

    Optional<Schematic> schematic(Id id) {
        return all.stream()
            .filter(schematic -> schematic.id().equals(id))
            .findAny();
    }

    private List<Schematic> schematicsInContructionOrder() {
        return all.stream()
            .filter(this::toBeConstructed)
            .sorted(this::comparedByMutualRequirement)
            .toList();
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
