package com.github.maximtereshchenko.conveyor.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

record PreferencesModel(
    LinkedHashSet<PreferencesInclusionModel> inclusions,
    LinkedHashSet<ArtifactPreferenceModel> artifacts
) {

    PreferencesModel override(PreferencesModel base) {
        return new PreferencesModel(
            reduce(base.inclusions(), inclusions, PreferencesInclusionModel::idModel),
            reduce(base.artifacts(), artifacts, ArtifactPreferenceModel::idModel)
        );
    }

    private <T> LinkedHashSet<T> reduce(
        Set<T> first,
        Set<T> second,
        Function<T, IdModel> classifier
    ) {
        return Stream.of(first, second)
            .flatMap(Collection::stream)
            .collect(new ReducingCollector<>(classifier));
    }
}
