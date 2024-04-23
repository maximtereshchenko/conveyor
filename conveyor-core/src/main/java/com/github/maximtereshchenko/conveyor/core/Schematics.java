package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.common.api.Product;
import com.github.maximtereshchenko.conveyor.common.api.Stage;

import java.util.*;

final class Schematics {

    private final LinkedHashSet<Schematic> all;
    private final Schematic initial;

    Schematics(LinkedHashSet<Schematic> all, Schematic initial) {
        this.all = all;
        this.initial = initial;
    }

    void construct(Stage stage) {
        var products = Set.<Product>of();
        for (var schematic : schematicsInConstructionOrder()) {
            products = schematic.construct(products, stage(stage, schematic));
        }
    }

    Optional<Schematic> schematic(Id id) {
        return all.stream()
            .filter(schematic -> schematic.id().equals(id))
            .findAny();
    }

    private List<Schematic> schematicsInConstructionOrder() {
        var schematics = new ArrayList<Schematic>();
        for (var schematic : all) {
            if (toBeConstructed(schematic)) {
                schematics.add(insertionIndex(schematic, schematics), schematic);
            }
        }
        return schematics;
    }

    private int insertionIndex(Schematic schematic, List<Schematic> schematics) {
        for (int i = 0; i < schematics.size(); i++) {
            if (schematics.get(i).dependsOn(schematic, this)) {
                return i;
            }
        }
        return schematics.size();
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

    private boolean toBeConstructed(Schematic schematic) {
        return initial.inheritsFrom(schematic) ||
               schematic.inheritsFrom(initial) ||
               initial.dependsOn(schematic, this);
    }
}
