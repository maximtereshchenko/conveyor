package com.github.maximtereshchenko.conveyor.core;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class SemanticVersion implements Comparable<SemanticVersion> {

    private final String raw;

    SemanticVersion(String raw) {
        this.raw = raw;
    }

    @Override
    public int compareTo(SemanticVersion semanticVersion) {
        var byVersions = compareByVersions(semanticVersion);
        if (byVersions != 0) {
            return byVersions;
        }
        var thisPreReleaseIdentifiers = preReleaseIdentifiers();
        var otherPreReleaseIdentifiers = semanticVersion.preReleaseIdentifiers();
        var comparedByPreReleaseVersion = compareByPreReleaseVersion(
            thisPreReleaseIdentifiers,
            otherPreReleaseIdentifiers
        );
        if (comparedByPreReleaseVersion != 0) {
            return comparedByPreReleaseVersion;
        }
        return compareByPreReleaseIdentifiers(
            thisPreReleaseIdentifiers,
            otherPreReleaseIdentifiers
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        var that = (SemanticVersion) object;
        return Objects.equals(raw, that.raw);
    }

    @Override
    public String toString() {
        return raw;
    }

    private int compareByPreReleaseIdentifiers(
        List<String> preReleaseIdentifiers,
        List<String> otherPreReleaseIdentifiers
    ) {
        for (int i = 0; i < preReleaseIdentifiers.size() && i < otherPreReleaseIdentifiers.size(); i++) {
            var identifier = preReleaseIdentifiers.get(i);
            var otherIdentifier = otherPreReleaseIdentifiers.get(i);
            var comparedNumericallyOrLexically = compareNumericallyOrLexically(
                identifier,
                otherIdentifier
            );
            if (comparedNumericallyOrLexically != 0) {
                return comparedNumericallyOrLexically;
            }
        }
        return Integer.compare(preReleaseIdentifiers.size(), otherPreReleaseIdentifiers.size());
    }

    private int compareNumericallyOrLexically(String identifier, String otherIdentifier) {
        return number(identifier)
            .map(identifierNumber ->
                number(otherIdentifier)
                    .map(otherIdentifierNumber ->
                        Integer.compare(identifierNumber, otherIdentifierNumber)
                    )
                    .orElse(-1)
            )
            .orElseGet(() -> identifier.compareTo(otherIdentifier));
    }

    private Optional<Integer> number(String possibleNumber) {
        try {
            return Optional.of(Integer.valueOf(possibleNumber));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private int compareByPreReleaseVersion(
        List<String> preReleaseIdentifiers,
        List<String> otherPreReleaseIdentifiers
    ) {
        if (preReleaseIdentifiers.isEmpty() && !otherPreReleaseIdentifiers.isEmpty()) {
            return 1;
        }
        if (otherPreReleaseIdentifiers.isEmpty() && !preReleaseIdentifiers.isEmpty()) {
            return -1;
        }
        return 0;
    }

    private int compareByVersions(SemanticVersion semanticVersion) {
        return Comparator.comparingInt(SemanticVersion::major)
            .thenComparingInt(SemanticVersion::minor)
            .thenComparingInt(SemanticVersion::patch)
            .compare(this, semanticVersion);
    }

    private int major() {
        return versionNumbers().getFirst();
    }

    private int minor() {
        return versionNumbers().get(1);
    }

    private int patch() {
        var versionNumbers = versionNumbers();
        if (versionNumbers.size() == 3) {
            return versionNumbers.getLast();
        }
        return 0;
    }

    private List<String> preReleaseIdentifiers() {
        return dashSeparated()
            .skip(1L)
            .flatMap(this::dotSeparated)
            .toList();
    }

    private List<Integer> versionNumbers() {
        return dashSeparated()
            .limit(1L)
            .flatMap(this::dotSeparated)
            .map(Integer::valueOf)
            .toList();
    }

    private Stream<String> dashSeparated() {
        return separated(raw, '-');
    }

    private Stream<String> dotSeparated(String numbers) {
        return separated(numbers, '.');
    }

    private Stream<String> separated(String string, char separator) {
        return Stream.of(string.split("[%c]".formatted(separator)));
    }
}
