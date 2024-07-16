package com.github.maximtereshchenko.conveyor.core;

import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.api.BindingStage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Tasks {

    private static final Map<BindingStage, BindingStage> STAGE_DEPENDENCIES = Map.of(
        BindingStage.PUBLISH, BindingStage.ARCHIVE,
        BindingStage.ARCHIVE, BindingStage.TEST,
        BindingStage.TEST, BindingStage.COMPILE
    );

    private final List<Task> all;

    Tasks(List<Task> all) {
        this.all = all;
    }

    @Override
    public String toString() {
        return all.toString();
    }

    void execute(List<Stage> stages) {
        stages.stream()
            .map(this::activeStages)
            .forEach(activeStages -> all.forEach(task -> task.execute(activeStages)));
    }

    private Set<BindingStage> activeStages(Stage stage) {
        return Stream.iterate(
                BindingStage.valueOf(stage.toString()),
                Objects::nonNull,
                STAGE_DEPENDENCIES::get
            )
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(BindingStage.class)));
    }
}
