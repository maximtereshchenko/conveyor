package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.Stage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

final class Schematics {

    private final LinkedHashSet<Schematic> all;
    private final Schematic initial;
    private final Tracer tracer;

    Schematics(LinkedHashSet<Schematic> all, Schematic initial, Tracer tracer) {
        this.all = all;
        this.initial = initial;
        this.tracer = tracer;
    }

    void construct(List<Stage> stages) {
        var schematicsInConstructionOrder = schematicsInConstructionOrder();
        tracer.submitConstructionOrder(schematicsInConstructionOrder);
        var constructionRepository = new ConstructionRepository();
        for (var schematic : schematicsInConstructionOrder) {
            schematic.construct(constructionRepository, stages);
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

    private boolean toBeConstructed(Schematic schematic) {
        return initial.inheritsFrom(schematic) ||
               schematic.inheritsFrom(initial) ||
               initial.dependsOn(schematic, this);
    }
}
