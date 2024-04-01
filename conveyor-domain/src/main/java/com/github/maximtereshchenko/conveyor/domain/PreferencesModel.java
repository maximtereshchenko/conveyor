package com.github.maximtereshchenko.conveyor.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record PreferencesModel(
    Set<PreferencesInclusionModel> inclusions,
    Set<ArtifactPreferenceModel> artifacts
) {

    PreferencesModel override(PreferencesModel base) {
        return new PreferencesModel(
            reduce(base.inclusions(), inclusions, PreferencesInclusionModel::name),
            reduce(base.artifacts(), artifacts, ArtifactPreferenceModel::name)
        );
    }

    private <T> Set<T> reduce(Set<T> first, Set<T> second, Function<T, String> classifier) {
        return new HashSet<>(
            Stream.of(first, second)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(classifier, Function.identity(), (present, next) -> next))
                .values()
        );
    }
}
