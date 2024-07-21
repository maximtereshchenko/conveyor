package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.Stage;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Schematics {

    private final LinkedHashMap<Id, Schematic> indexed;
    private final Schematic initial;
    private final Executor executor;

    private Schematics(
        LinkedHashMap<Id, Schematic> indexed,
        Schematic initial,
        Executor executor
    ) {
        this.indexed = indexed;
        this.initial = initial;
        this.executor = executor;
    }

    static Schematics from(
        LinkedHashSet<Schematic> schematics,
        Schematic initial,
        Executor executor
    ) {
        return new Schematics(
            schematics.stream()
                .collect(
                    Collectors.toMap(
                        Schematic::id,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                    )
                ),
            initial,
            executor
        );
    }

    void construct(List<Stage> stages) {
        var constructionRepository = new ConstructionRepository();
        var constructions = new HashMap<Id, CompletableFuture<Void>>();
        for (var schematic : indexed.values()) {
            if (toBeConstructed(schematic)) {
                construct(schematic, constructions, constructionRepository, stages);
            }
        }
        awaitConstruction(constructions.values());
    }

    Optional<Schematic> schematic(Id id) {
        return Optional.ofNullable(indexed.get(id));
    }

    private void awaitConstruction(Collection<CompletableFuture<Void>> futures) {
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw e;
        }
    }

    private CompletableFuture<Void> construct(
        Schematic schematic,
        Map<Id, CompletableFuture<Void>> constructions,
        ConstructionRepository constructionRepository,
        List<Stage> stages
    ) {
        if (constructions.containsKey(schematic.id())) {
            return constructions.get(schematic.id());
        }
        var future = CompletableFuture.allOf(
                schematic.required()
                    .stream()
                    .filter(indexed::containsKey)
                    .map(id -> construct(
                        indexed.get(id),
                        constructions,
                        constructionRepository,
                        stages
                    ))
                    .toArray(CompletableFuture[]::new)
            )
            .thenRunAsync(
                () -> schematic.construct(constructionRepository, stages),
                executor
            );
        constructions.put(schematic.id(), future);
        return future;
    }

    private boolean toBeConstructed(Schematic schematic) {
        return initial.inheritsFrom(schematic) ||
               schematic.inheritsFrom(initial) ||
               initial.dependsOn(schematic, this);
    }
}
