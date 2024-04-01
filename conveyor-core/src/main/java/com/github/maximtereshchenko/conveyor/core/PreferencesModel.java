package com.github.maximtereshchenko.conveyor.core;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

record PreferencesModel(
    Set<PreferencesInclusionModel> inclusions,
    Set<ArtifactPreferenceModel> artifacts
) {

    PreferencesModel override(PreferencesModel base) {
        return new PreferencesModel(
            reduce(base.inclusions(), inclusions, PreferencesInclusionModel::id),
            reduce(base.artifacts(), artifacts, ArtifactPreferenceModel::id)
        );
    }

    private <T> Set<T> reduce(Set<T> first, Set<T> second, Function<T, Id> classifier) {
        return Stream.of(first, second)
            .flatMap(Collection::stream)
            .collect(new OverridingCollector<>(classifier, (next, existing) -> next));
    }
}
